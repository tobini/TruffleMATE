package som.primitives.arithmetic;

import java.math.BigInteger;

import com.oracle.truffle.api.source.SourceSection;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import tools.dym.Tags.BasicPrimitiveOperation;
import tools.dym.Tags.OpArithmetic;


public abstract class ArithmeticPrim extends BinaryExpressionNode {
  public ArithmeticPrim(final boolean eagWrap, SourceSection source) {
    super(eagWrap, source);
  }

  protected final Number reduceToLongIfPossible(final BigInteger result) {
    if (result.bitLength() > Long.SIZE - 1) {
      return result;
    } else {
      return result.longValue();
    }
  }

  @Override
  protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
    if (tag == OpArithmetic.class || tag == BasicPrimitiveOperation.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
