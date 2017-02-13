package som.interpreter.nodes;

import som.interpreter.nodes.FieldNode.FieldReadNode;
import som.interpreter.nodes.FieldNode.FieldWriteNode;
import som.interpreter.objectstorage.MateLayoutFieldReadNode;
import som.interpreter.objectstorage.MateLayoutFieldWriteNode;
import som.matenodes.IntercessionHandling;
import som.vm.constants.ReflectiveOp;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;


public abstract class MateFieldNodes {
  public abstract static class MateFieldReadNode extends FieldReadNode {
    @Child private IntercessionHandling ih;

    public MateFieldReadNode(FieldReadNode node) {
      super(node.read.getFieldIndex(), node.getSourceSection());
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorReadField);
      read = new MateLayoutFieldReadNode(read);
      this.adoptChildren();
    }

    @Override
    @Specialization
    public Object executeEvaluated(final VirtualFrame frame, final DynamicObject obj) {
      Object value = ih.doMateSemantics(frame, new Object[] {obj, (long) this.read.getFieldIndex()});
      if (value == null) {
       value = ((MateLayoutFieldReadNode) read).read(frame, obj);
      }
      return value;
    }

    @Override
    public ExpressionNode asMateNode() {
      return null;
    }
  }

  public abstract static class MateFieldWriteNode extends FieldWriteNode {
    @Child private IntercessionHandling ih;

    public MateFieldWriteNode(FieldWriteNode node) {
      super(node.write.getFieldIndex(), node.getSourceSection());
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorWriteField);
      write = new MateLayoutFieldWriteNode(write);
      this.adoptChildren();
    }

    @Override
    @Specialization
    public final Object executeEvaluated(final VirtualFrame frame,
        final DynamicObject self, final Object value) {
      Object val = ih.doMateSemantics(frame, new Object[] {self, (long) this.write.getFieldIndex(), value});
      if (val == null) {
       val = ((MateLayoutFieldWriteNode) write).write(frame, self, value);
      }
      return val;
    }

    @Override
    public ExpressionNode asMateNode() {
      return null;
    }
  }
}
