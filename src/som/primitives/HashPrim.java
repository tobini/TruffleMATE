package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
public abstract class HashPrim extends UnaryExpressionNode {
  public HashPrim() {
    super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Hash"));
  }

  @Specialization
  public final long doString(final String receiver) {
    return receiver.hashCode();
  }

  @Specialization
  public final long doSSymbol(final SSymbol receiver) {
    return receiver.getString().hashCode();
  }

  @Specialization
  public final long doSAbstractObject(final SAbstractObject receiver) {
    return receiver.hashCode();
  }

  @Specialization
  public final long doDynamicObject(final DynamicObject receiver) {
    return receiver.hashCode();
  }
}
