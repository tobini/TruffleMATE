package som.interpreter.nodes.specialized.whileloops;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SBlock;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;


@GenerateNodeFactory
public abstract class WhilePrimitiveNode extends BinaryExpressionNode {
  final boolean predicateBool;
  @Child protected WhileCache whileNode;

  protected WhilePrimitiveNode(final boolean predicateBool, final ExecutionLevel level) {
    super(null);
    this.predicateBool = predicateBool;
    this.whileNode = WhileCacheNodeGen.create(predicateBool, null, null);
  }

  protected WhilePrimitiveNode(final WhilePrimitiveNode node, final ExecutionLevel level) {
    this(node.predicateBool, level);
  }

  @Specialization
  protected DynamicObject doWhileConditionally(final VirtualFrame frame,
      final SBlock loopCondition, final SBlock loopBody) {
    return (DynamicObject) whileNode.executeEvaluated(frame, loopCondition, loopBody);
  }

  public abstract static class WhileTruePrimitiveNode extends WhilePrimitiveNode {
    public WhileTruePrimitiveNode(final ExecutionLevel level) { super(true, level); }
  }

  public abstract static class WhileFalsePrimitiveNode extends WhilePrimitiveNode {
    public WhileFalsePrimitiveNode(final ExecutionLevel level) { super(false, level); }
  }
}
