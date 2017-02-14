package som.interpreter.objectstorage;

import som.interpreter.objectstorage.FieldAccessorNode.WriteFieldNode;
import som.matenodes.IntercessionHandling;
import som.vm.Universe;
import som.vm.constants.ReflectiveOp;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;


public final class MateLayoutFieldWriteNode extends WriteFieldNode {
  @Child private IntercessionHandling ih;
  @Child private WriteFieldNode write;

  public MateLayoutFieldWriteNode(final WriteFieldNode node) {
    super(node.getFieldIndex());
    ih = IntercessionHandling.createForOperation(ReflectiveOp.LayoutWriteField);
    write = node;
    this.adoptChildren();
  }

  public Object write(final VirtualFrame frame, final DynamicObject receiver, final Object value) {
    Object val = ih.doMateSemantics(frame, new Object[] {receiver, (long) this.getFieldIndex(), value});
    if (val == null) {
     val = write.executeWrite(receiver, value);
    }
    return val;
  }

  @Override
  public Object executeWrite(DynamicObject obj, Object value) {
    /*Should never enter here*/
    assert (false);
    Universe.errorExit("Mate enters an unexpected method");
    return value;
  }
}
