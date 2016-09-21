package som.primitives.arithmetic;

import java.math.BigInteger;

import som.interpreter.SomLanguage;
import som.vm.NotYetImplementedException;
import som.vmobjects.SAbstractObject;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.Source;


@GenerateNodeFactory
public abstract class DoubleDivPrim extends ArithmeticPrim {
  public DoubleDivPrim() {
    super(Source.newBuilder("/").internal().name("divide double").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
  }

  @Specialization
  public final double doDouble(final double left, final double right) {
    return left / right;
  }

  @Specialization
  public final double doLong(final long left, final long right) {
    return ((double) left) / right;
  }

  @Specialization
  public final double doDouble(final double left, final long right) {
    return doDouble(left, (double) right);
  }

  @Specialization
  public final SAbstractObject doLong(final long left, final BigInteger right) {
    CompilerAsserts.neverPartOfCompilation("DoubleDiv100");
    throw new NotYetImplementedException(); // TODO: need to implement the "/" case here directly... : return resendAsBigInteger("/", left, (SBigInteger) rightObj, frame.pack());
  }

  @Specialization
  public final double doLong(final long left, final double right) {
    return left / right;
  }
}
