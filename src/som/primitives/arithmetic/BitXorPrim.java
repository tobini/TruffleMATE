package som.primitives.arithmetic;

import som.primitives.Primitive;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@Primitive(klass = "Integer", selector = "bitXor:")
public abstract class BitXorPrim extends ArithmeticPrim {
  public BitXorPrim(final boolean eagWrap, final SourceSection source) {
    super(eagWrap, source);
  }

  @Specialization
  public final long doLong(final long receiver, final long right) {
    return receiver ^ right;
  }
}
