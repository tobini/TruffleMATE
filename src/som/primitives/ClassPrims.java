package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SClass;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


public class ClassPrims {

  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class NamePrim extends UnaryExpressionNode {
    public NamePrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Name"));
    }

    @TruffleBoundary
    @Specialization(guards = "isSClass(receiver)")
    public final SAbstractObject doSClass(final DynamicObject receiver) {
      //CompilerAsserts.neverPartOfCompilation("Class>>NamePrim");
      return SClass.getName(receiver);
    }
  }

  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class SuperClassPrim extends UnaryExpressionNode {
    public SuperClassPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Superclass"));
    }

    @Specialization(guards = "isSClass(receiver)")
    public final Object doSClass(final DynamicObject receiver) {
      CompilerAsserts.neverPartOfCompilation("Class>>SuperClassPrim");
      return SClass.getSuperClass(receiver);
    }
  }

  
  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class InstanceInvokablesPrim extends UnaryExpressionNode {
    public InstanceInvokablesPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Instance Invokables"));
    }

    @TruffleBoundary
    @Specialization(guards = "isSClass(receiver)")
    public final SArray doSClass(final DynamicObject receiver) {
      CompilerAsserts.neverPartOfCompilation("Class>>InstanceInvokablesPrim");
      return SClass.getInstanceInvokables(receiver);
    }
  }

  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class InstanceFieldsPrim extends UnaryExpressionNode {
    public InstanceFieldsPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Instance Fields"));
    }

    @Specialization(guards = "isSClass(receiver)")
    public final SArray doSClass(final DynamicObject receiver) {
      CompilerAsserts.neverPartOfCompilation("Class>>instanceFields");
      return SClass.getInstanceFields(receiver);
    }
  }
}
