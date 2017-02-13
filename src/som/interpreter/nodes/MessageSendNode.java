package som.interpreter.nodes;

import som.instrumentation.MessageSendNodeWrapper;
import som.interpreter.SArguments;
import som.interpreter.TruffleCompiler;
import som.interpreter.nodes.dispatch.AbstractDispatchNode;
import som.interpreter.nodes.dispatch.DispatchChain.Cost;
import som.interpreter.nodes.dispatch.GenericDispatchNode;
import som.interpreter.nodes.dispatch.SuperDispatchNode;
import som.interpreter.nodes.dispatch.UninitializedDispatchNode;
import som.interpreter.nodes.nary.EagerlySpecializableNode;
import som.interpreter.nodes.nary.ExpressionWithReceiver;
import som.interpreter.nodes.nary.ExpressionWithTagsNode;
import som.primitives.Primitives;
import som.primitives.Primitives.Specializer;
import som.vm.NotYetImplementedException;
import som.vm.Universe;
import som.vm.constants.MateClasses;
import som.vmobjects.SSymbol;
import tools.dym.Tags.VirtualInvoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;
import com.oracle.truffle.api.instrumentation.StandardTags.CallTag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.source.SourceSection;

public final class MessageSendNode {

  public static AbstractMessageSendNode create(final SSymbol selector,
      final ExpressionNode[] arguments, final SourceSection source) {
    return new UninitializedMessageSendNode(selector, arguments, source);
  }

  public static AbstractMessageSendNode createForPerformNodes(final SSymbol selector) {
    return new UninitializedSymbolSendNode(selector, null);
  }

  public static GenericMessageSendNode createGeneric(final SSymbol selector,
      final ExpressionNode[] argumentNodes, final SourceSection source) {
    if (Universe.getCurrent().vmReflectionEnabled()) {
      return new MateGenericMessageSendNode(selector, argumentNodes,
          new UninitializedDispatchNode(source, selector), source);
    } else {
      return new GenericMessageSendNode(selector, argumentNodes,
          new UninitializedDispatchNode(source, selector), source);
    }
  }

  public abstract static class AbstractMessageSendNode extends ExpressionWithTagsNode
      implements PreevaluatedExpression, ExpressionWithReceiver {

    public static AbstractMessageSpecializationsFactory specializationFactory = new AbstractMessageSpecializationsFactory.SOMMessageSpecializationsFactory();
    @Children protected final ExpressionNode[] argumentNodes;

    protected AbstractMessageSendNode(final ExpressionNode[] arguments,
        final SourceSection source) {
      super(source);
      this.argumentNodes = arguments;
    }

    public boolean isSuperSend() {
      return argumentNodes[0] instanceof ISuperReadNode;
    }

    @Override
    public ExpressionNode getReceiver() {
      return argumentNodes[0];
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      Object[] arguments = evaluateArguments(frame);
      return doPreEvaluated(frame, arguments);
    }

    public Object[] evaluateArguments(final VirtualFrame frame) {
      Object receiver = argumentNodes[0].executeGeneric(frame);
      return evaluateArgumentsWithReceiver(frame, receiver);
    }

    @Override
    public Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver) {
      Object[] arguments = evaluateArgumentsWithReceiver(frame, receiver);
      return doPreEvaluated(frame, arguments);
    }

    @ExplodeLoop
    public Object[] evaluateArgumentsWithReceiver(final VirtualFrame frame, final Object receiver) {
      Object[] arguments = new Object[argumentNodes.length];
      arguments[0] = receiver;
      for (int i = 1; i < argumentNodes.length; i++) {
        arguments[i] = argumentNodes[i].executeGeneric(frame);
        assert arguments[i] != null;
      }
      return arguments;
    }

    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == CallTag.class) {
        return true;
      }
      return super.isTaggedWith(tag);
    }
  }

  public abstract static class AbstractUninitializedMessageSendNode
      extends AbstractMessageSendNode {

    protected final SSymbol selector;

    public SSymbol getSelector() {
      return this.selector;
    }

    protected AbstractUninitializedMessageSendNode(final SSymbol selector,
        final ExpressionNode[] arguments, final SourceSection source) {
      super(arguments, source);
      this.selector = selector;
    }

    @Override
    public final Object doPreEvaluated(final VirtualFrame frame,
        final Object[] arguments) {
      return specialize(arguments, frame).
          doPreEvaluated(frame, arguments);
    }

    protected PreevaluatedExpression specialize(final Object[] arguments, final VirtualFrame frame) {
      TruffleCompiler.transferToInterpreterAndInvalidate("Specialize Message Node");

      if (isSuperSend()) {
        return makeSuperSend();
      }

      Primitives prims = Universe.getCurrent().getPrimitives();

      Specializer<EagerlySpecializableNode> specializer = prims.getEagerSpecializer(selector,
          arguments, argumentNodes);

      // synchronized (getLock()) {
      if (specializer != null) {
        EagerlySpecializableNode newNode = specializer.create(arguments, argumentNodes, getSourceSection(), !specializer.noWrapper(), frame);
        if (specializer.noWrapper()) {
          return replace(newNode);
        } else {
          return makeEagerPrim(newNode);
        }
      }
      return makeGenericSend();
      // }
    }


    protected abstract PreevaluatedExpression makeSuperSend();

    protected GenericMessageSendNode makeGenericSend() {
      return replace(new GenericMessageSendNode(selector,
          argumentNodes,
          new UninitializedDispatchNode(this.sourceSection, selector),
          getSourceSection()));
    }

    private PreevaluatedExpression makeEagerPrim(final EagerlySpecializableNode prim) {
      Universe.insertInstrumentationWrapper(this);
      PreevaluatedExpression result = replace(prim.wrapInEagerWrapper(prim, selector, argumentNodes));
      Universe.insertInstrumentationWrapper((Node) result);
      for (ExpressionNode arg: argumentNodes) {
        unwrapIfNecessary(arg).markAsPrimitiveArgument();
        Universe.insertInstrumentationWrapper(arg);
      }
      return result;
    }
  }

  @Instrumentable(factory = MessageSendNodeWrapper.class)
  public static class UninitializedMessageSendNode
      extends AbstractUninitializedMessageSendNode implements PreevaluatedExpression{

    protected UninitializedMessageSendNode(final SSymbol selector,
        final ExpressionNode[] arguments, final SourceSection source) {
      super(selector, arguments, source);
    }

    @Override
    protected PreevaluatedExpression makeSuperSend() {
      ISuperReadNode argumentNode;
      argumentNode = (ISuperReadNode) (argumentNodes[0]);
      GenericMessageSendNode node = new GenericMessageSendNode(selector,
        argumentNodes, SuperDispatchNode.create(this.sourceSection, selector,
            argumentNode), getSourceSection());
      return replace(node);
    }

    protected UninitializedMessageSendNode(final UninitializedMessageSendNode wrappedNode) {
      super(wrappedNode.selector, null, null);
    }

    @Override
    public Node asMateNode() {
      return new MateUninitializedMessageSendNode(this);
    }
  }

  private static final class UninitializedSymbolSendNode
    extends AbstractUninitializedMessageSendNode {

    protected UninitializedSymbolSendNode(final SSymbol selector,
        final SourceSection source) {
      super(selector, new ExpressionNode[selector.getNumberOfSignatureArguments()], source);
      // super(selector, new ExpressionNode[0], source);
    }

    @Override
    public boolean isSuperSend() {
      // TODO: is is correct?
      return false;
    }

    @Override
    protected PreevaluatedExpression makeSuperSend() {
      // should never be reached with isSuperSend() returning always false
      throw new NotYetImplementedException();
    }

    /*
     * There is a problem with the specialization of reflective nodes.
     * TODO: fix! 
     */
    @Override
    protected PreevaluatedExpression specialize(final Object[] arguments, final VirtualFrame frame) {
      /*switch (selector.getString()) {
        case "whileTrue:": {
          if (arguments[1] instanceof SBlock && arguments[0] instanceof SBlock) {
            SBlock argBlock = (SBlock) arguments[1];
            return replace(new WhileWithDynamicBlocksNode((SBlock) arguments[0],
                argBlock, true, getSourceSection(), SArguments.getExecutionLevel(frame)));
          }
          break;
        }
        case "whileFalse:":
          if (arguments[1] instanceof SBlock && arguments[0] instanceof SBlock) {
            SBlock    argBlock     = (SBlock)    arguments[1];
            return replace(new WhileWithDynamicBlocksNode(
                (SBlock) arguments[0], argBlock, false, getSourceSection(), SArguments.getExecutionLevel(frame)));
          }
          break; // use normal send
      }

      return super.specialize(arguments, frame);*/
      return this.makeGenericSend();
    }

    @Override
    protected GenericMessageSendNode makeGenericSend() {
      // TODO: figure out what to do with reflective sends and how to instrument them.
      GenericMessageSendNode send = new GenericMessageSendNode(selector,
          argumentNodes,
          new UninitializedDispatchNode(getSourceSection(), selector),
          getSourceSection());
      return replace(send);
    }
  }

  @Instrumentable(factory = MessageSendNodeWrapper.class)
  public static class GenericMessageSendNode
      extends AbstractMessageSendNode {

    protected final SSymbol selector;
    @Child private AbstractDispatchNode dispatchNode;

    protected GenericMessageSendNode(final SSymbol selector,
        final ExpressionNode[] arguments,
        final AbstractDispatchNode dispatchNode, final SourceSection source) {
      super(arguments, source);
      this.selector = selector;
      this.dispatchNode = dispatchNode;
      this.adoptChildren();
    }

    @Override
    public Object doPreEvaluated(final VirtualFrame frame,
        final Object[] arguments) {
      return dispatchNode.executeDispatch(frame, MateClasses.STANDARD_ENVIRONMENT, SArguments.getExecutionLevel(frame), arguments);
    }

    public void replaceDispatchListHead(
        final GenericDispatchNode replacement) {
      CompilerAsserts.neverPartOfCompilation("GenericMessageSendNode.replaceDispatchListHead");
      dispatchNode.replace(replacement);
    }

    public AbstractDispatchNode getDispatchListHead() {
      return dispatchNode;
    }

    @Override
    public String toString() {
      return "GMsgSend(" + selector.getString() + ")";
    }

    @Override
    public NodeCost getCost() {
      return Cost.getCost(dispatchNode);
    }

    public SSymbol getSelector() {
      return this.selector;
    }

    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == VirtualInvoke.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  public static class CascadeMessageSendNode
      extends ExpressionWithTagsNode {
    @Child private ExpressionNode receiver;
    final @Children private ExpressionWithReceiver[] messages;

    public CascadeMessageSendNode(final ExpressionNode receiver,
        final ExpressionWithReceiver[] messages, final SourceSection source) {

      super(source);
      this.receiver = receiver;
      this.messages = messages;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(final VirtualFrame frame) {
      Object rcvr = receiver.executeGeneric(frame);

      for (int i = 0; i < messages.length - 1; i++) {
        this.messages[i].executeGenericWithReceiver(frame, rcvr);
      }

      return this.messages[messages.length - 1].executeGenericWithReceiver(frame, rcvr);
    }
  }
}
