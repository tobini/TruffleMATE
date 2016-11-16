package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInterface;

public interface ExpressionWithReceiver extends NodeInterface {
  public abstract ExpressionNode getReceiver();
  public abstract Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver);
}
