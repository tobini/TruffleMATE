package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SClass;
import tools.dym.Tags.NewObject;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@ImportStatic(SClass.class)
public abstract class NewObjectPrim extends UnaryExpressionNode {
  public NewObjectPrim() {
    super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "New"));
  }

  @Specialization(guards = "receiver == cachedClass")
  public final DynamicObject cachedClass(final DynamicObject receiver,
      @Cached("receiver") final DynamicObject cachedClass,
      @Cached("getFactory(cachedClass)") final DynamicObjectFactory factory) {
    //The parameter is only valid for SReflectiveObjects
    return factory.newInstance();
  }

  @TruffleBoundary
  @Specialization(contains = "cachedClass")
  public DynamicObject uncached(final DynamicObject receiver) {
    return SClass.getFactory(receiver).newInstance();
  }
  
  @Override
  protected boolean isTaggedWith(final Class<?> tag) {
    if (tag == NewObject.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}

