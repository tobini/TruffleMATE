package som.interpreter.nodes;

import som.interpreter.SArguments;
import som.interpreter.nodes.ArgumentReadNode.LocalArgumentReadNode;
import som.interpreter.nodes.ArgumentReadNode.LocalSuperReadNode;
import som.interpreter.nodes.ArgumentReadNode.NonLocalArgumentReadNode;
import som.interpreter.nodes.ArgumentReadNode.NonLocalSuperReadNode;
import som.matenodes.IntercessionHandling;
import som.vm.constants.ReflectiveOp;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

public abstract class MateArgumentReadNode {
  public static class MateLocalArgumentReadNode extends LocalArgumentReadNode{
    @Child private IntercessionHandling ih;

    public MateLocalArgumentReadNode(int argumentIndex, SourceSection source) {
      super(argumentIndex, source);
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorLocalArg);
      this.adoptChildren();
    }

    public MateLocalArgumentReadNode(LocalArgumentReadNode node) {
      this(node.argumentIndex, node.getSourceSection());
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      Object value = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame)});
      if (value == null) {
       value = super.executeGeneric(frame);
      }
      return value;
    }

    @Override
    public ExpressionNode asMateNode() {
      return null;
    }
  }

  public static class MateNonLocalArgumentReadNode extends NonLocalArgumentReadNode{
    @Child private IntercessionHandling ih;

    public MateNonLocalArgumentReadNode(int argumentIndex, int contextLevel,
        SourceSection source) {
      super(argumentIndex, contextLevel, source);
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorNonLocalArg);
      this.adoptChildren();
    }

    public MateNonLocalArgumentReadNode(NonLocalArgumentReadNode node) {
      this(node.argumentIndex, node.contextLevel, node.getSourceSection());
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      Object value = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame)});
      if (value == null) {
       value = super.executeGeneric(frame);
      }
      return value;
    }

    @Override
    public ExpressionNode asMateNode() {
      return null;
    }
  }

  public static final class MateLocalSuperReadNode extends LocalSuperReadNode implements
      ISuperReadNode{
    @Child private IntercessionHandling ih;

    public MateLocalSuperReadNode(SSymbol holderClass, boolean classSide,
        SourceSection source) {
      super(holderClass, classSide, source);
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorLocalSuperArg);
      this.adoptChildren();
    }

    public MateLocalSuperReadNode(LocalSuperReadNode node) {
      this(node.getHolderClass(), node.isClassSide(), node.getSourceSection());
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      Object value = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame)});
      if (value == null) {
       value = super.executeGeneric(frame);
      }
      return value;
    }

    @Override
    public ExpressionNode asMateNode() {
      return null;
    }
  }

  public static final class MateNonLocalSuperReadNode extends NonLocalSuperReadNode implements
      ISuperReadNode {
    @Child private IntercessionHandling ih;

    public MateNonLocalSuperReadNode(int contextLevel, SSymbol holderClass,
        boolean classSide, SourceSection source) {
      super(contextLevel, holderClass, classSide, source);
      ih = IntercessionHandling.createForOperation(ReflectiveOp.ExecutorNonLocalSuperArg);
      this.adoptChildren();
    }

    public MateNonLocalSuperReadNode(NonLocalSuperReadNode node) {
      this(node.getContextLevel(), node.getHolderClass(), node.isClassSide(), node.getSourceSection());
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      Object value = ih.doMateSemantics(frame, new Object[] {SArguments.rcvr(frame)});
      if (value == null) {
       value = super.executeGeneric(frame);
      }
      return value;
    }

    @Override
    public ExpressionNode asMateNode() {
      return null;
    }
  }
}
