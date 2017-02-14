package som.primitives;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.Primitives.Specializer;
import som.vm.constants.Classes;


public abstract class DoublePrims  {

  @GenerateNodeFactory
  @Primitive(klass = "Double", selector = "round", receiverType = Double.class)
  public abstract static class RoundPrim extends UnaryExpressionNode {
    public RoundPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final long doDouble(final double receiver) {
      return Math.round(receiver);
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Double", selector = "asInteger", receiverType = Double.class)
  public abstract static class AsIntegerPrim extends UnaryExpressionNode {
    public AsIntegerPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final long doDouble(final double receiver) {
      return (long) receiver;
    }
  }

  public static class IsDoubleClass extends Specializer<ExpressionNode> {
    public IsDoubleClass(final Primitive prim, final NodeFactory<ExpressionNode> fact) { super(prim, fact); }

    @Override
    public boolean matches(final Object[] args, final ExpressionNode[] argNodess) {
      return args[0] == Classes.doubleClass;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Double Class", selector = "PositiveInfinity",
             noWrapper = true, specializer = IsDoubleClass.class)
  public abstract static class PositiveInfinityPrim extends UnaryExpressionNode {
    public PositiveInfinityPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    protected final boolean receiverIsDoubleClass(final DynamicObject receiver) {
      return receiver == Classes.doubleClass;
    }

    @Specialization(guards = "receiverIsDoubleClass(receiver)")
    public final double doSClass(final DynamicObject receiver) {
      return Double.POSITIVE_INFINITY;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Double", selector = "floor", receiverType = Double.class)
  public abstract static class FloorPrim extends UnaryExpressionNode {
    public FloorPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final long doDouble(final double receiver) {
      return (long) Math.floor(receiver);
    }
  }
}
