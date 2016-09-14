package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public interface ExpressionWithReceiver {
  public abstract ExpressionNode getReceiver();
  public abstract Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver);
}
