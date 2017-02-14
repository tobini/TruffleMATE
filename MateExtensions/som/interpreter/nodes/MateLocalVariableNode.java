package som.interpreter.nodes;

import som.interpreter.SArguments;
import som.interpreter.nodes.LocalVariableNode.LocalVariableReadNode;
import som.interpreter.nodes.LocalVariableNode.LocalVariableWriteNode;
import som.matenodes.IntercessionHandling;
import som.vm.constants.ReflectiveOp;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class MateLocalVariableNode {
  public static class MateLocalVariableReadNode extends LocalVariableReadNode {

    public MateLocalVariableReadNode(LocalVariableReadNode node) {
      super(node);
      this.local = node;
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorReadLocal);
      this.adoptChildren();
    }

    @Child private IntercessionHandling ih;
    @Child LocalVariableNode local;

    @Override
    public Object executeGeneric(VirtualFrame frame) {
      Object value = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame)});
      if (value == null) {
       value = local.executeGeneric(frame);
      }
      return value;
    }
  }

  public static class MateLocalVariableWriteNode extends LocalVariableWriteNode {

    @Child private IntercessionHandling ih;
    @Child LocalVariableWriteNode local;

    public MateLocalVariableWriteNode(LocalVariableWriteNode node) {
      super(node);
      this.local = node;
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorWriteLocal);
      this.adoptChildren();
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
      Object value = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame)});
      if (value == null) {
       value = local.executeGeneric(frame);
      }
      return value;
    }

    @Override
    public ExpressionNode getExp() {
      return local.getExp();
    }
  }
}
