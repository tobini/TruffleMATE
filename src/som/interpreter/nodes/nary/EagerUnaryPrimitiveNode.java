package som.interpreter.nodes.nary;

import som.interpreter.TruffleCompiler;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.MessageSendNode;
import som.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrument.WrapperNode;

public class EagerUnaryPrimitiveNode extends EagerPrimitive {

  @Child private ExpressionNode receiver;
  @Child private UnaryExpressionNode primitive;

  private final SSymbol selector;

  public ExpressionNode getReceiver(){
    return receiver;
  }
  
  public EagerUnaryPrimitiveNode(final SSymbol selector,
      final ExpressionNode receiver, final UnaryExpressionNode primitive) {
    super(primitive.getSourceSection());
    this.receiver  = receiver;
    this.primitive = primitive;
    this.selector = selector;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    Object rcvr = receiver.executeGeneric(frame);
    return executeEvaluated(frame, rcvr);
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
  
  protected SSymbol getSelector(){
    return selector;
  }
  
  @Override
  protected boolean isTaggedWith(final Class<?> tag) {
    assert !(primitive instanceof WrapperNode);
    return primitive.isTaggedWith(tag);
  }

  @Override
  public String getOperation() {
    return selector.getString();
  }

  @Override
  public Object doPreEvaluated(VirtualFrame frame, Object[] args) {
    return executeEvaluated(frame, args[0]);
  }
}
