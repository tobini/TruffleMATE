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

public class EagerQuaternaryPrimitiveNode extends EagerPrimitive {

  @Child private ExpressionNode receiver;
  @Child private ExpressionNode argument1;
  @Child private ExpressionNode argument2;
  @Child private ExpressionNode argument3;
  @Child private QuaternaryExpressionNode primitive;

  public EagerQuaternaryPrimitiveNode(
      final SSymbol selector,
      final ExpressionNode receiver,
      final ExpressionNode argument1,
      final ExpressionNode argument2,
      final ExpressionNode argument3,
      final QuaternaryExpressionNode primitive) {
    super(primitive.getSourceSection(), selector);
    this.receiver  = receiver;
    this.argument1 = argument1;
    this.argument2 = argument2;
    this.argument3 = argument3;
    this.primitive = primitive;
  }

  public ExpressionNode getReceiver() { return receiver; }
  protected ExpressionNode getFirstArg() { return argument1; }
  protected ExpressionNode getSecondArg() { return argument2; }
  protected ExpressionNode getPrimitive() { return primitive; }

  @Override
  public Object executeGenericWithReceiver(final VirtualFrame frame, Object receiver) {
    Object arg1 = argument1.executeGeneric(frame);
    Object arg2 = argument2.executeGeneric(frame);
    Object arg3 = argument3.executeGeneric(frame);

    return executeEvaluated(frame, receiver, arg1, arg2, arg3);
  }

  public Object executeEvaluated(final VirtualFrame frame,
    final Object receiver, final Object argument1, final Object argument2, final Object argument3) {
    try {
      return primitive.executeEvaluated(frame, receiver, argument1, argument2, argument3);
    } catch (UnsupportedSpecializationException e) {
      TruffleCompiler.transferToInterpreterAndInvalidate("Eager Primitive with unsupported specialization.");
      return makeGenericSend().doPreEvaluated(frame,
          new Object[] {receiver, argument1, argument2, argument3});
    }
  }

  private AbstractMessageSendNode makeGenericSend() {
    GenericMessageSendNode node = MessageSendNode.createGeneric(selector,
        new ExpressionNode[] {receiver, argument1, argument2, argument3},
        getSourceSection());
    return replace(node);
  }

  public ExpressionNode getThirdArg() {
    return argument3;
  }

  @Override
  protected boolean isTaggedWith(final Class<?> tag) {
    assert !(primitive instanceof WrapperNode);
    boolean result = super.isTaggedWith(tag) ? super.isTaggedWith(tag) : primitive.isTaggedWith(tag);
    return result;
  }

  @Override
  public Object doPreEvaluated(VirtualFrame frame, Object[] args) {
    return executeEvaluated(frame, args[0], args[1], args[2], args[3]);
  }

  @Override
  protected void setTags(final byte tagMark) {
    primitive.tagMark = tagMark;
  }
}
