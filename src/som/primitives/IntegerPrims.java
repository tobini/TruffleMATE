package som.primitives;

import java.math.BigInteger;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.arithmetic.ArithmeticPrim;
import som.vm.constants.Classes;
import som.vmobjects.SArray;
import som.vmobjects.SSymbol;
import tools.dym.Tags.ComplexPrimitiveOperation;
import tools.dym.Tags.OpArithmetic;
import tools.dym.Tags.StringAccess;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;


public abstract class IntegerPrims {

  @GenerateNodeFactory
  public abstract static class RandomPrim extends UnaryExpressionNode {
    public RandomPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Random"));
    }

    @Specialization
    public final long doLong(final long receiver) {
      return (long) (receiver * Math.random());
    }
  }

  @GenerateNodeFactory
  public abstract static class As32BitSignedValue extends UnaryExpressionNode {
    public As32BitSignedValue() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "As32bit"));
    }

    @Specialization
    public final long doLong(final long receiver) {
      return (int) receiver;
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == OpArithmetic.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  public abstract static class As32BitUnsignedValue extends UnaryExpressionNode {
    public As32BitUnsignedValue() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "As32BitUnsigned"));
    }

    @Specialization
    public final long doLong(final long receiver) {
      return Integer.toUnsignedLong((int) receiver);
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == OpArithmetic.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  public abstract static class FromStringPrim extends ArithmeticPrim {
    public FromStringPrim() {
      super(Source.newBuilder("FromString").internal().name("from string for integers").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    protected final boolean receiverIsIntegerClass(final DynamicObject receiver) {
      return receiver == Classes.integerClass;
    }

    @Specialization(guards = "receiverIsIntegerClass(receiver)")
    public final Object doSClass(final DynamicObject receiver, final String argument) {
      return Long.parseLong(argument);
    }

    @Specialization(guards = "receiverIsIntegerClass(receiver)")
    public final Object doSClass(final DynamicObject receiver, final SSymbol argument) {
      return Long.parseLong(argument.getString());
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == ComplexPrimitiveOperation.class) {
        return true;
      } else if (tag == StringAccess.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  public abstract static class LeftShiftPrim extends ArithmeticPrim {
    public LeftShiftPrim() {
      super(Source.newBuilder("<<").internal().name("left shift").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    private final BranchProfile overflow = BranchProfile.create();

    @Specialization(rewriteOn = ArithmeticException.class)
    public final long doLong(final long receiver, final long right) {
      assert right >= 0;  // currently not defined for negative values of right

      if (Long.SIZE - Long.numberOfLeadingZeros(receiver) + right > Long.SIZE - 1) {
        overflow.enter();
        throw new ArithmeticException("shift overflows long");
      }
      return receiver << right;
    }

    @Specialization
    public final BigInteger doLongWithOverflow(final long receiver, final long right) {
      assert right >= 0;  // currently not defined for negative values of right
      assert right <= Integer.MAX_VALUE;

      return BigInteger.valueOf(receiver).shiftLeft((int) right);
    }
  }

  @GenerateNodeFactory
  public abstract static class UnsignedRightShiftPrim extends ArithmeticPrim {
    public UnsignedRightShiftPrim() {
      super(Source.newBuilder(">>").internal().name("unsigned right shift").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final long doLong(final long receiver, final long right) {
      return receiver >>> right;
    }
  }

  @GenerateNodeFactory
  public abstract static class MaxIntPrim extends ArithmeticPrim {
    public MaxIntPrim() {
      super(Source.newBuilder("MaxInt").internal().name("max integer").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final long doLong(final long receiver, final long right) {
      return Math.max(receiver, right);
    }
  }

  @GenerateNodeFactory
  public abstract static class ToPrim extends BinaryExpressionNode {
    public ToPrim() {
      super(Source.newBuilder("To for Arrays").internal().name("to").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final SArray doLong(final long receiver, final long right) {
      int cnt = (int) right - (int) receiver + 1;
      long[] arr = new long[cnt];
      for (int i = 0; i < cnt; i++) {
        arr[i] = i + receiver;
      }
      return SArray.create(arr);
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == OpArithmetic.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  public abstract static class AbsPrim extends UnaryExpressionNode {
    public AbsPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Abs"));
    }

    @Specialization
    public final long doLong(final long receiver) {
      return Math.abs(receiver);
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == OpArithmetic.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }
}
