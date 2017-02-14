package som.instrumentation;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.PreevaluatedExpression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableFactory;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.NodeCost;

public final class FixedSizeExpressionWrapperFactory implements
    InstrumentableFactory<ExpressionNode> {

  @Override
  public com.oracle.truffle.api.instrumentation.InstrumentableFactory.WrapperNode createWrapper(
      ExpressionNode delegateNode, ProbeNode probeNode) {
    return new FixedSizeWrapper(delegateNode, probeNode);
  }

  private static final class FixedSizeWrapper extends ExpressionNode
      implements WrapperNode {

    @Child private ExpressionNode delegateNode;
    @Child private ProbeNode      probeNode;

    FixedSizeWrapper(final ExpressionNode delegateNode,
        final ProbeNode probeNode) {
      super(delegateNode.getSourceSection());
      this.delegateNode = delegateNode;
      this.probeNode = probeNode;
    }

    @Override
    public ExpressionNode getDelegateNode() {
      return delegateNode;
    }

    @Override
    public ProbeNode getProbeNode() {
      return probeNode;
    }

    @Override
    public NodeCost getCost() {
      return NodeCost.NONE;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      try {
        probeNode.onEnter(frame);
        Object result = delegateNode.executeGeneric(frame);
        probeNode.onReturnValue(frame, result);
        return result;
      } catch (Throwable t) {
        probeNode.onReturnExceptional(frame, t);
        throw t;
      }
    }

    @SuppressWarnings("unused")
    public Object doPreEvaluated(final VirtualFrame frame, final Object[] args) {
      try {
        probeNode.onEnter(frame);
        Object result = ((PreevaluatedExpression) delegateNode).doPreEvaluated(frame, args);
        probeNode.onReturnValue(frame, result);
        return result;
      } catch (Throwable t) {
        probeNode.onReturnExceptional(frame, t);
        throw t;
      }
    }
  }
}
