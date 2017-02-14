package som.interpreter.nodes.specialized.whileloops;

import java.util.List;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.literals.BlockNode;
import som.interpreter.nodes.specialized.whileloops.WhileWithStaticBlocksNode.WhileFalseSplzr;
import som.interpreter.nodes.specialized.whileloops.WhileWithStaticBlocksNode.WhileTrueSplzr;
import som.primitives.Primitive;
import som.primitives.Primitives.Specializer;
import som.vm.NotYetImplementedException;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SBlock;

import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@Primitive(selector = "whileTrue:",  noWrapper = true, specializer = WhileTrueSplzr.class)
@Primitive(selector = "whileFalse:", noWrapper = true, specializer = WhileFalseSplzr.class)
public final class WhileWithStaticBlocksNode extends AbstractWhileNode {
  @Child protected BlockNode receiver;
  @Child protected BlockNode argument;

  public abstract static class WhileSplzr extends Specializer<WhileWithStaticBlocksNode> {
    private final boolean whileTrueOrFalse;
    protected WhileSplzr(final Primitive prim,
        final NodeFactory<WhileWithStaticBlocksNode> fact,
        final boolean whileTrueOrFalse) {
      super(prim, fact);
      this.whileTrueOrFalse = whileTrueOrFalse;
    }

    @Override
    public boolean matches(final Object[] args,
        final ExpressionNode[] argNodes) {
      return unwrapIfNecessary(argNodes[1]) instanceof BlockNode &&
          unwrapIfNecessary(argNodes[0]) instanceof BlockNode;
    }

    @Override
    public WhileWithStaticBlocksNode create(final Object[] arguments,
        final ExpressionNode[] argNodes, final SourceSection section,
        final boolean eagerWrapper, final Frame frame) {
      assert !eagerWrapper;
      BlockNode argBlockNode = (BlockNode) unwrapIfNecessary(argNodes[1]);
      SBlock    argBlock     = (SBlock)    arguments[1];
      return new WhileWithStaticBlocksNode(
          (BlockNode) unwrapIfNecessary(argNodes[0]), argBlockNode,
          (SBlock) arguments[0], argBlock, whileTrueOrFalse, section, (ExecutionLevel) arguments[0]);
    }
  }

  public static final class WhileTrueSplzr extends WhileSplzr {
    public WhileTrueSplzr(final Primitive prim,
        final NodeFactory<WhileWithStaticBlocksNode> fact) { super(prim, fact, true); }
  }

  public static final class WhileFalseSplzr extends WhileSplzr {
    public WhileFalseSplzr(final Primitive prim,
        final NodeFactory<WhileWithStaticBlocksNode> fact) { super(prim, fact, false); }
  }

  @Override
  public ExpressionNode getReceiver() {
    return receiver;
  }

  private WhileWithStaticBlocksNode(final BlockNode receiver,
      final BlockNode argument, final SBlock rcvr, final SBlock arg,
      final boolean predicateBool, final SourceSection source, ExecutionLevel level) {
    super(rcvr, arg, predicateBool, source, level);
    this.receiver = receiver;
    this.argument = argument;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    SBlock rcvr = receiver.executeSBlock(frame);
    return executeGenericWithReceiver(frame, rcvr);
  }

  @Override
  public Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver) {
    SBlock arg  = argument.executeSBlock(frame);
    return executeEvaluated(frame, receiver, arg);
  }

  @Override
  protected DynamicObject doWhileConditionally(final VirtualFrame frame,
      final SBlock loopCondition,
      final SBlock loopBody) {
    return doWhileUnconditionally(frame, loopCondition, loopBody);
  }

  public static final class WhileWithStaticBlocksNodeFactory implements NodeFactory<WhileWithStaticBlocksNode> {

    @Override
    public WhileWithStaticBlocksNode createNode(final Object... args) {
      return new WhileWithStaticBlocksNode((BlockNode) args[0],
          (BlockNode) args[1], (SBlock) args[2], (SBlock) args[3],
          (Boolean) args[4], (SourceSection) args[5], (ExecutionLevel) args[6]);
    }

    @Override
    public Class<WhileWithStaticBlocksNode> getNodeClass() {
      return WhileWithStaticBlocksNode.class;
    }

    @Override
    public List<List<Class<?>>> getNodeSignatures() {
      throw new NotYetImplementedException();
    }

    @Override
    public List<Class<? extends Node>> getExecutionSignature() {
      throw new NotYetImplementedException();
    }
  }
}
