package som.interpreter.nodes.literals;

import som.compiler.MethodGenerationContext;
import som.compiler.Variable.Local;
import som.interpreter.InlinerAdaptToEmbeddedOuterContext;
import som.interpreter.InlinerForLexicallyEmbeddedMethods;
import som.interpreter.Invokable;
import som.interpreter.Method;
import som.interpreter.SplitterForLexicallyEmbeddedCode;
import som.interpreter.nodes.ExpressionNode;
import som.vm.Universe;
import som.vmobjects.InvokableLayoutImpl;
import som.vmobjects.MethodLayoutImpl;
import som.vmobjects.SBlock;
import som.vmobjects.SInvokable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public class BlockNode extends LiteralNode {

  protected final DynamicObject blockMethod;
  @CompilationFinal protected DynamicObject blockClass;

  public BlockNode(final DynamicObject blockMethod,
      final SourceSection source) {
    super(source);
    if ((Universe.getCurrent().vmReflectionEnabled())){
      Universe.getCurrent().mateifyMethod(blockMethod);
    }
    this.blockMethod = blockMethod;
  }

  protected void setBlockClass() {
    switch (SInvokable.getNumberOfArguments(blockMethod)) {
      case 1: blockClass = Universe.getCurrent().getBlockClass(1); break;
      case 2: blockClass = Universe.getCurrent().getBlockClass(2); break;
      case 3: blockClass = Universe.getCurrent().getBlockClass(3); break;
      case 4: blockClass = Universe.getCurrent().getBlockClass(4); break;
      default:
        throw new RuntimeException("We do currently not have support for more than 3 arguments to a block.");
    }
  }

  @Override
  public SBlock executeSBlock(final VirtualFrame frame) {
    if (blockClass == null) {
      CompilerDirectives.transferToInterpreter();
      setBlockClass();
    }
    return Universe.newBlock(blockMethod, blockClass, null);
  }

  @Override
  public final Object executeGeneric(final VirtualFrame frame) {
    return executeSBlock(frame);
  }

  @Override
  public void replaceWithIndependentCopyForInlining(final SplitterForLexicallyEmbeddedCode inliner) {
    Invokable clonedInvokable = InvokableLayoutImpl.INSTANCE.getInvokable(blockMethod).
        cloneWithNewLexicalContext(inliner.getCurrentScope());
    replaceAdapted(clonedInvokable);
  }

  @Override
  public void replaceWithLexicallyEmbeddedNode(
      final InlinerForLexicallyEmbeddedMethods inliner) {
    Invokable adapted = ((Method) InvokableLayoutImpl.INSTANCE.getInvokable(blockMethod)).
        cloneAndAdaptToEmbeddedOuterContext(inliner);
    replaceAdaptedForEmbeddedOuter(adapted, inliner.getBlockArguments());
  }

  @Override
  public void replaceWithCopyAdaptedToEmbeddedOuterContext(
      final InlinerAdaptToEmbeddedOuterContext inliner) {
    Invokable adapted = ((Method) InvokableLayoutImpl.INSTANCE.getInvokable(blockMethod)).
        cloneAndAdaptToSomeOuterContextBeingEmbedded(inliner);
    replaceAdapted(adapted);
  }

  private void replaceAdapted(final Invokable adaptedForContext) {
    DynamicObject adapted = methodForInvokable(adaptedForContext);
    replace(createNode(adapted));
  }
  
  private DynamicObject methodForInvokable(final Invokable adaptedForContext){
    return Universe.newMethod(
        MethodLayoutImpl.INSTANCE.getSignature(blockMethod), adaptedForContext, false,
        MethodLayoutImpl.INSTANCE.getEmbeddedBlocks(blockMethod));
  }
  
  private void replaceAdaptedForEmbeddedOuter(final Invokable adaptedForContext, Local[] blockArguments) {
    DynamicObject adapted = methodForInvokable(adaptedForContext);
    replace(createNodeForEmbeddedOuter(adapted, blockArguments));
  }

  protected BlockNode createNode(final DynamicObject adapted) {
    return new BlockNode(adapted, getSourceSection());
  }
  
  protected BlockNode createNodeForEmbeddedOuter(final DynamicObject adapted, final Local[] blockArguments){
    return createNode(adapted);
  }

  @Override
  public ExpressionNode inline(final MethodGenerationContext mgenc,
      final Local... blockArguments) {
    // self doesn't need to be passed
    assert SInvokable.getNumberOfArguments(blockMethod) - 1 == blockArguments.length;
    return InvokableLayoutImpl.INSTANCE.getInvokable(blockMethod).inline(mgenc, blockArguments);
  }

  public static class BlockNodeWithContext extends BlockNode {

    public BlockNodeWithContext(final DynamicObject blockMethod,
        final SourceSection source) {
      super(blockMethod, source);
    }

    public BlockNodeWithContext(final BlockNodeWithContext node) {
      this(node.blockMethod, node.getSourceSection());
    }

    @Override
    public SBlock executeSBlock(final VirtualFrame frame) {
      if (blockClass == null) {
        CompilerDirectives.transferToInterpreter();
        setBlockClass();
      }
      return Universe.newBlock(blockMethod, blockClass, frame.materialize());
    }

    @Override
    protected BlockNode createNode(final DynamicObject adapted) {
      return new BlockNodeWithContext(adapted, getSourceSection());
    }
    
    @Override
    protected BlockNode createNodeForEmbeddedOuter(final DynamicObject adapted, final Local[] blockArguments){
      return new BlockNodeWithContextForInlinedScope(adapted, getSourceSection(), blockArguments);
    }
  }
  
  public static final class BlockNodeWithContextForInlinedScope extends BlockNodeWithContext {
    private final Local[] inlinedAsLocalBlockArguments;
    
    public BlockNodeWithContextForInlinedScope(final DynamicObject blockMethod,
        final SourceSection source, final Local[] blockArguments) {
      super(blockMethod, source);
      this.inlinedAsLocalBlockArguments = blockArguments;
    }
    
    @Override
    public SBlock executeSBlock(final VirtualFrame frame) {
      if (blockClass == null) {
        CompilerDirectives.transferToInterpreter();
        setBlockClass();
      }
      FrameDescriptor descriptor = new FrameDescriptor();
      VirtualFrame blockArgumentsEmbeddedFrame = Universe.getCurrent().getTruffleRuntime().createVirtualFrame(null, descriptor);
      try {
        for (Local local: inlinedAsLocalBlockArguments){
          FrameSlot slot = blockArgumentsEmbeddedFrame.getFrameDescriptor().addFrameSlot(local.getSlotIdentifier(), local.getSlot().getKind());
          switch(local.getSlot().getKind()){
            case Long:
                blockArgumentsEmbeddedFrame.setLong(slot, frame.getLong(local.getSlot()));
                break;
            case Boolean:
              assert false;
              break;
            case Byte:
              assert false;
              break;
            case Double:
              assert false;
              break;
            case Float:
              assert false;
              break;
            case Int:
              assert false;
              break;
            case Object:
              assert false;
              break;
            default:
              assert false;
              break;
          }
        }
      } catch (FrameSlotTypeException e) {
        // Should never enter here!
      }
      return Universe.newBlock(blockMethod, blockClass, frame.materialize(), blockArgumentsEmbeddedFrame.materialize());
    }
  }

}