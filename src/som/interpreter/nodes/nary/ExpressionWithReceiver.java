package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInterface;

public interface ExpressionWithReceiver extends NodeInterface {
  ExpressionNode getReceiver();
  Object executeGenericWithReceiver(VirtualFrame frame, Object receiver);
}
