package som.primitives;

import java.math.BigInteger;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;


@GenerateNodeFactory
public abstract class AsStringPrim extends UnaryExpressionNode {

  @Specialization
  public final String doSSymbol(final SSymbol receiver) {
    return receiver.getString();
  }
  
  @Specialization
  public final String doCharacter(final char receiver) {
    return Character.toString(receiver);
  }

  @TruffleBoundary
  @Specialization
  public final String doLong(final long receiver) {
    return Long.toString(receiver);
  }

  @TruffleBoundary
  @Specialization
  public final String doDouble(final double receiver) {
    return Double.toString(receiver);
  }

  @TruffleBoundary
  @Specialization
  public final String doBigInteger(final BigInteger receiver) {
    return receiver.toString();
  }
}
