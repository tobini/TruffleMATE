package som.interpreter.nodes.nary;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

import som.interpreter.nodes.ExpressionWithReceiverNode;
import som.interpreter.nodes.OperationNode;
import som.interpreter.nodes.PreevaluatedExpression;


public abstract class EagerPrimitive extends ExpressionWithReceiverNode
    implements OperationNode, PreevaluatedExpression {

  protected EagerPrimitive(final SourceSection source) {
    super(source);
  }
  
  @Override
  public Object executeGeneric(final VirtualFrame frame){
    return executeGenericWithReceiver(frame, this.getReceiver().executeGeneric(frame));
  }

  /*protected EagerPrimitive(final EagerPrimitive prim) {
    super(prim);
  }

  protected abstract void setTags(byte tagMark);*/
}