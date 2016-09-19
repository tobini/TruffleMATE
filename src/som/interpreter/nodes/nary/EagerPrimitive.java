package som.interpreter.nodes.nary;

import som.interpreter.nodes.OperationNode;
import som.interpreter.nodes.PreevaluatedExpression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


public abstract class EagerPrimitive extends ExpressionWithTagsNode
    implements OperationNode, ExpressionWithReceiver, PreevaluatedExpression {

  protected EagerPrimitive(final SourceSection source) {
    super(source);
  }
  
  @Override
  public Object executeGeneric(final VirtualFrame frame){
    return executeGenericWithReceiver(frame, this.getReceiver().executeGeneric(frame));
  }

  protected abstract void setTags(byte tagMark);
  /*protected EagerPrimitive(final EagerPrimitive prim) {
    super(prim);
  }*/
}