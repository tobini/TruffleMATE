package som.interpreter.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

public abstract class ExpressionWithReceiverNode extends ExpressionWithTagsNode {
  public ExpressionWithReceiverNode(final SourceSection sourceSection) {
    super(sourceSection);
  }

  public abstract ExpressionNode getReceiver();
  public abstract Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver);
}
