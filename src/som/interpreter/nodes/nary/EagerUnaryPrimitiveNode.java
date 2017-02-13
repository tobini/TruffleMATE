package som.interpreter.nodes.nary;

import som.interpreter.TruffleCompiler;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.MessageSendNode;
import som.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableFactory.WrapperNode;


public class EagerUnaryPrimitiveNode extends EagerPrimitive {

  @Child private ExpressionNode receiver;
  @Child private UnaryExpressionNode primitive;

  public ExpressionNode getReceiver() { return receiver; }
  protected ExpressionNode getPrimitive() { return primitive; }

  public EagerUnaryPrimitiveNode(final SSymbol selector,
      final ExpressionNode receiver, final UnaryExpressionNode primitive) {
    super(primitive.getSourceSection(), selector);
    this.receiver  = receiver;
    this.primitive = primitive;
    this.adoptChildren();
  }

  @Override
  public Object executeGenericWithReceiver(final VirtualFrame frame, Object receiver) {
    return executeEvaluated(frame, receiver);
  }

  public Object executeEvaluated(final VirtualFrame frame,
      final Object receiver) {
    try {
      return primitive.executeEvaluated(frame, receiver);
    } catch (UnsupportedSpecializationException e) {
      TruffleCompiler.transferToInterpreterAndInvalidate("Eager Primitive with unsupported specialization.");
      return makeGenericSend().doPreEvaluated(frame, new Object[] {receiver});
    }
  }

  private GenericMessageSendNode makeGenericSend() {
    GenericMessageSendNode node = MessageSendNode.createGeneric(selector,
        new ExpressionNode[] {receiver}, getSourceSection());
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
    return executeEvaluated(frame, args[0]);
  }

  @Override
  protected void setTags(final byte tagMark) {
    primitive.tagMark = tagMark;
  }
}
