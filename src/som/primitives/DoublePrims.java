package som.primitives;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.constants.Classes;


public abstract class DoublePrims  {

  @GenerateNodeFactory
  public abstract static class RoundPrim extends UnaryExpressionNode {
    public RoundPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Round"));
    }

    @Specialization
    public final long doDouble(final double receiver) {
      return Math.round(receiver);
    }
  }

  @GenerateNodeFactory
  public abstract static class AsIntegerPrim extends UnaryExpressionNode {
    public AsIntegerPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "AsInteger"));
    }

    @Specialization
    public final long doDouble(final double receiver) {
      return (long) receiver;
    }
  }

  @GenerateNodeFactory
  public abstract static class PositiveInfinityPrim extends UnaryExpressionNode {
    public PositiveInfinityPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Positive Infinity"));
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
  public abstract static class FloorPrim extends UnaryExpressionNode {
    public FloorPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Floor"));
    }

    @Specialization
    public final long doDouble(final double receiver) {
      return (long) Math.floor(receiver);
    }
  }
}