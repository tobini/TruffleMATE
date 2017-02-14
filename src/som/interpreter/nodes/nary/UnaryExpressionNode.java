package som.interpreter.nodes.nary;

import som.instrumentation.FixedSizeExpressionWrapperFactory;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.PreevaluatedExpression;
import som.interpreter.nodes.MessageSendNode.AbstractMessageSendNode;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;
import com.oracle.truffle.api.source.SourceSection;


@Instrumentable(factory = FixedSizeExpressionWrapperFactory.class)
@NodeChild(value = "receiver", type = ExpressionNode.class)
public abstract class UnaryExpressionNode extends EagerlySpecializableNode
    implements ExpressionWithReceiver, PreevaluatedExpression {

  public UnaryExpressionNode(final boolean eagerlyWrapped,
      final SourceSection source) {
    super(eagerlyWrapped, source);
  }

  public abstract Object executeEvaluated(VirtualFrame frame,
      Object receiver);

  @Override
  public final Object doPreEvaluated(final VirtualFrame frame,
      final Object[] arguments) {
    return executeEvaluated(frame, arguments[0]);
  }

  public Object[] evaluateArguments(final VirtualFrame frame) {
    Object[] arguments = new Object[1];
    arguments[0] = this.getReceiver().executeGeneric(frame);
    return arguments;
  }

  public EagerPrimitive wrapInEagerWrapper(
      final EagerlySpecializableNode prim, final SSymbol selector,
      final ExpressionNode[] arguments) {
    return AbstractMessageSendNode.specializationFactory.unaryPrimitiveFor(selector,
        arguments[0], this);
  }
}
