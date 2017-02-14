package som.interpreter.nodes.nary;

import som.interpreter.TruffleCompiler;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.MessageSendNode;
import som.interpreter.nodes.MessageSendNode.AbstractMessageSendNode;
import som.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableFactory.WrapperNode;

public class EagerTernaryPrimitiveNode extends EagerPrimitive {

  @Child private ExpressionNode receiver;
  @Child private ExpressionNode argument1;
  @Child private ExpressionNode argument2;
  @Child private TernaryExpressionNode primitive;

  public EagerTernaryPrimitiveNode(
      final SSymbol selector,
      final ExpressionNode receiver,
      final ExpressionNode argument1,
      final ExpressionNode argument2,
      final TernaryExpressionNode primitive) {
    super(primitive.getSourceSection(), selector);
    this.receiver  = receiver;
    this.argument1 = argument1;
    this.argument2 = argument2;
    this.primitive = primitive;
    // this.adoptChildren();
  }

  public ExpressionNode getReceiver() { return receiver; }
  protected ExpressionNode getFirstArg() { return argument1; }
  protected ExpressionNode getSecondArg() { return argument2; }
  protected TernaryExpressionNode getPrimitive() { return primitive; }

  @Override
  public Object executeGenericWithReceiver(final VirtualFrame frame, Object receiver) {
    Object arg1 = argument1.executeGeneric(frame);
    Object arg2 = argument2.executeGeneric(frame);

    return executeEvaluated(frame, receiver, arg1, arg2);
  }

  public Object executeEvaluated(final VirtualFrame frame,
    final Object receiver, final Object argument1, final Object argument2) {
    try {
      return primitive.executeEvaluated(frame, receiver, argument1, argument2);
    } catch (UnsupportedSpecializationException e) {
      TruffleCompiler.transferToInterpreterAndInvalidate("Eager Primitive with unsupported specialization.");
      return makeGenericSend().doPreEvaluated(frame,
          new Object[] {receiver, argument1, argument2});
    }
  }

  private AbstractMessageSendNode makeGenericSend() {
    GenericMessageSendNode node = MessageSendNode.createGeneric(selector,
        new ExpressionNode[] {receiver, argument1, argument2},
        getSourceSection());
    return replace(node);
  }

  @Override
  protected boolean isTaggedWith(final Class<?> tag) {
    assert !(primitive instanceof WrapperNode);
    boolean result = super.isTaggedWith(tag) ? super.isTaggedWith(tag) : primitive.isTaggedWith(tag);
    return result;
  }

  @Override
  public Object doPreEvaluated(VirtualFrame frame, Object[] args) {
    return executeEvaluated(frame, args[0], args[1], args[2]);
  }

  @Override
  protected void setTags(final byte tagMark) {
    primitive.tagMark = tagMark;
  }
}
