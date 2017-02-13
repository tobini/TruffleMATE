package som.interpreter.nodes.specialized.whileloops;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.primitives.Primitive;
import som.vmobjects.SBlock;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
public abstract class WhilePrimitiveNode extends BinaryExpressionNode {
  final boolean predicateBool;
  @Child protected WhileCache whileNode;

  protected WhilePrimitiveNode(final boolean eagPrim, final SourceSection source, final boolean predicateBool) {
    super(false, source);
    this.predicateBool = predicateBool;
    this.whileNode = WhileCacheNodeGen.create(predicateBool, null, null);
  }

  @Specialization
  protected DynamicObject doWhileConditionally(final VirtualFrame frame,
      final SBlock loopCondition, final SBlock loopBody) {
    return (DynamicObject) whileNode.executeEvaluated(frame, loopCondition, loopBody);
  }

  @Primitive(klass = "Block", selector = "whileTrue:",
             eagerSpecializable = false, receiverType = {SBlock.class})
  public abstract static class WhileTruePrimitiveNode extends WhilePrimitiveNode {
    public WhileTruePrimitiveNode(final boolean eagPrim, final SourceSection source) { super(eagPrim, source, true); }
  }

  @Primitive(klass = "Block", selector = "whileFalse:",
             eagerSpecializable = false, receiverType = {SBlock.class})
  public abstract static class WhileFalsePrimitiveNode extends WhilePrimitiveNode {
    public WhileFalsePrimitiveNode(final boolean eagPrim, final SourceSection source) { super(eagPrim, source, false); }
  }

  @Override
  protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
    if (tag == LoopNode.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
