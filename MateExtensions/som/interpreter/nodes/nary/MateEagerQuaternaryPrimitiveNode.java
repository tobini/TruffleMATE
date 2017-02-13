package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;
import som.matenodes.IntercessionHandling;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;

public class MateEagerQuaternaryPrimitiveNode extends EagerQuaternaryPrimitiveNode {
  @Child private IntercessionHandling messageSend;
  @Child private IntercessionHandling primitiveActivation;

  public MateEagerQuaternaryPrimitiveNode(SSymbol selector, ExpressionNode receiver, ExpressionNode argument1, ExpressionNode argument2,
      ExpressionNode argument3, QuaternaryExpressionNode primitive) {
    super(selector, receiver, argument1, argument2, argument3, primitive);
    messageSend = IntercessionHandling.createForMessageLookup(this.getSelector());
    primitiveActivation = IntercessionHandling.createForOperation(this.getPrimitive().reflectiveOperation());
    this.adoptChildren();
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    Object rcvr = this.getReceiver().executeGeneric(frame);
    Object arg1 = this.getFirstArg().executeGeneric(frame);
    Object arg2 = this.getSecondArg().executeGeneric(frame);
    Object arg3 = this.getThirdArg().executeGeneric(frame);
    return this.doPreEvaluated(frame, new Object[] {rcvr, arg1, arg2, arg3});
  }

  @Override
  public Object doPreEvaluated(VirtualFrame frame, Object[] args) {
    Object value = messageSend.doMateSemantics(frame, args);
    if (value == null) {
     value = executeEvaluated(frame, args[0], args[1], args[2], args[3]);
    }
    return value;
  }

  @Override
  public Object executeEvaluated(final VirtualFrame frame,
      final Object receiver, final Object argument1, final Object argument2, final Object argument3) {
    Object value = primitiveActivation.doMateSemantics(frame, new Object[]{receiver, argument1, argument2, argument3});
    if (value == null) {
     value = super.executeEvaluated(frame, receiver, argument1, argument2, argument3);
    }
    return value;
  }
}
