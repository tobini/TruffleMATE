package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;
import som.matenodes.IntercessionHandling;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;


public class MateEagerBinaryPrimitiveNode extends EagerBinaryPrimitiveNode{
  @Child private IntercessionHandling messageSend;
  @Child private IntercessionHandling primitiveActivation;

  public MateEagerBinaryPrimitiveNode(SSymbol selector, ExpressionNode receiver, ExpressionNode argument,
      BinaryExpressionNode primitive) {
    super(selector, receiver, argument, primitive);
    messageSend = IntercessionHandling.createForMessageLookup(this.getSelector());
    primitiveActivation = IntercessionHandling.createForOperation(this.getPrimitive().reflectiveOperation());
    this.adoptChildren();
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    Object rcvr = this.getReceiver().executeGeneric(frame);
    Object arg  = this.getArgument().executeGeneric(frame);
    return this.doPreEvaluated(frame, new Object[] {rcvr, arg});
  }

  @Override
  public Object doPreEvaluated(VirtualFrame frame, Object[] args) {
    Object value = messageSend.doMateSemantics(frame, args);
    if (value == null) {
     value = executeEvaluated(frame, args[0], args[1]);
    }
    return value;
  }

  @Override
  public Object executeEvaluated(final VirtualFrame frame,
      final Object receiver, final Object argument1) {
    Object value = primitiveActivation.doMateSemantics(frame, new Object[]{receiver, argument1});
    if (value == null) {
     value = super.executeEvaluated(frame, receiver, argument1);
    }
    return value;
  }
}
