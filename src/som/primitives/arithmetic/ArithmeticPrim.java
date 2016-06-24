package som.primitives.arithmetic;

import java.math.BigInteger;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import tools.dym.Tags.OpArithmetic;


public abstract class ArithmeticPrim extends BinaryExpressionNode {
  protected final Number reduceToLongIfPossible(final BigInteger result) {
    if (result.bitLength() > Long.SIZE - 1) {
      return result;
    } else {
      return result.longValue();
    }
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
