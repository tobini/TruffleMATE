package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.PreevaluatedExpression;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;
import com.oracle.truffle.api.source.SourceSection;

import som.instrumentation.FixedSizeExpressionWrapperFactory;


@NodeChildren({
  @NodeChild(value = "receiver", type = ExpressionNode.class),
  @NodeChild(value = "argument", type = ExpressionNode.class)})
@Instrumentable(factory = FixedSizeExpressionWrapperFactory.class)
public abstract class BinaryExpressionNode extends ExpressionWithTagsNode
    implements ExpressionWithReceiver, PreevaluatedExpression {

  public abstract ExpressionNode getArgument();
  
  public BinaryExpressionNode(final SourceSection source) {
    super(source);
  }

  public abstract Object executeEvaluated(final VirtualFrame frame,
      final Object receiver, Object argument);

  @Override
  public Object doPreEvaluated(final VirtualFrame frame,
      final Object[] arguments) {
    return executeEvaluated(frame, arguments[0], arguments[1]);
  }
  
  //Weird, I need this method because if they do not exist eager classes do not compile
  @Override
  protected boolean isTaggedWith(final Class<?> tag) {
    return super.isTaggedWith(tag);
  }
}
