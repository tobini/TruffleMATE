package som.interpreter.nodes.nary;

import som.instrumentation.FixedSizeExpressionWrapperFactory;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.PreevaluatedExpression;
import som.vm.constants.ReflectiveOp;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;
import com.oracle.truffle.api.source.SourceSection;


@Instrumentable(factory = FixedSizeExpressionWrapperFactory.class)
@NodeChild(value = "receiver", type = ExpressionNode.class)
public abstract class UnaryExpressionNode extends ExpressionWithTagsNode
    implements ExpressionWithReceiver, PreevaluatedExpression {
  
  public UnaryExpressionNode(final SourceSection source) {
    super(source);
  }
  
  public abstract Object executeEvaluated(final VirtualFrame frame,
      final Object receiver);

  @Override
  public final Object doPreEvaluated(final VirtualFrame frame,
      final Object[] arguments) {
    return executeEvaluated(frame, arguments[0]);
  }
  
  public Object[] evaluateArguments(final VirtualFrame frame){
    Object[] arguments = new Object[1];
    arguments[0] = this.getReceiver().executeGeneric(frame);
    return arguments; 
  }
  
  public ReflectiveOp reflectiveOperation(){
    return ReflectiveOp.MessageLookup;
  }
}
