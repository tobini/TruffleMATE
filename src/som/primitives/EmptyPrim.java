package som.primitives;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class EmptyPrim extends UnaryExpressionNode {

  private EmptyPrim(final ExpressionNode receiver) {
    super(false, null);
  }

  public EmptyPrim(final EmptyPrim node) { this(node.getReceiver()); }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    return executeEvaluated(frame, null);
  }

  @Override
  public Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver) {
    return executeGeneric(frame);
  }

  @Override
  public Object executeEvaluated(final VirtualFrame frame, final Object receiver) {
    Universe.println("Warning: undefined primitive called");
    return null;
  }

  public static EmptyPrim create(final ExpressionNode receiver) {
    return new EmptyPrim(receiver);
  }

  @Override
  public ExpressionNode getReceiver() {
    return null;
  }
}
