package som.interpreter.nodes.nary;

import som.interpreter.nodes.OperationNode;
import som.interpreter.nodes.PreevaluatedExpression;
import som.vm.constants.ReflectiveOp;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


public abstract class EagerPrimitive extends ExpressionWithTagsNode
    implements OperationNode, ExpressionWithReceiver, PreevaluatedExpression {
  protected final SSymbol selector;

  protected EagerPrimitive(final SourceSection source, SSymbol sel) {
    super(source);
    selector = sel;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    return executeGenericWithReceiver(frame, this.getReceiver().executeGeneric(frame));
  }

  protected abstract void setTags(byte tagMark);

  protected SSymbol getSelector() {
    return selector;
  }

   @Override
  public String getOperation() {
    return selector.getString();
  }

  public ReflectiveOp reflectiveOperation() {
    return ReflectiveOp.MessageLookup;
  }
}
