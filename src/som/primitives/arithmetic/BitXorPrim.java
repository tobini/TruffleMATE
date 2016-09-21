package som.primitives.arithmetic;

import som.interpreter.SomLanguage;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.Source;


@GenerateNodeFactory
public abstract class BitXorPrim extends ArithmeticPrim {
  public BitXorPrim() {
    super(Source.newBuilder("Xor").internal().name("xor").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
  }

  @Specialization
  public final long doLong(final long receiver, final long right) {
    return receiver ^ right;
  }
}
