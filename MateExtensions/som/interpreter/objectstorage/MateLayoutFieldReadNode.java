package som.interpreter.objectstorage;

import som.interpreter.objectstorage.FieldAccessorNode.ReadFieldNode;
import som.matenodes.IntercessionHandling;
import som.vm.Universe;
import som.vm.constants.ReflectiveOp;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;


public final class MateLayoutFieldReadNode extends ReadFieldNode {
  @Child private IntercessionHandling ih;
  @Child private ReadFieldNode read;

  public MateLayoutFieldReadNode(final ReadFieldNode node) {
    super(node.getFieldIndex());
    ih = IntercessionHandling.createForOperation(ReflectiveOp.LayoutReadField);
    read = node;
    this.adoptChildren();
  }

  public Object read(final VirtualFrame frame, final DynamicObject receiver) {
    Object value = ih.doMateSemantics(frame, new Object[] {receiver, (long) this.getFieldIndex()});
    if (value == null) {
     value = read.executeRead(receiver);
    }
    return value;
  }

  @Override
  public Object executeRead(DynamicObject obj) {
    /*Should never enter here*/
    assert (false);
    Universe.errorExit("Mate enters an unexpected method");
    return null;
  }
}
