package som.interpreter.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import som.interpreter.SArguments;
import som.interpreter.nodes.nary.ExpressionWithTagsNode;
import som.matenodes.IntercessionHandling;
import som.vm.constants.ReflectiveOp;

public class MateReturnNode extends ExpressionWithTagsNode {
  @Child private IntercessionHandling ih;
  @Child ExpressionNode expression;

  public MateReturnNode(ExpressionNode node) {
    super(node.getSourceSection());
    expression = node;
    ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorReturn);
    this.adoptChildren();
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object value = expression.executeGeneric(frame);
    Object valueRedefined = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame), value});
    if (valueRedefined == null) {
      return value;
    }
    return valueRedefined;
  }
}
