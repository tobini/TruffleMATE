package som.primitives;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.Primitives.Specializer;
import som.vm.Universe;
import som.vm.constants.MateClasses;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SObject;
import som.vmobjects.SReflectiveObjectLayoutImpl.SReflectiveObjectType;
import som.vmobjects.SShape;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public final class MatePrims {
  @GenerateNodeFactory
  @Primitive(klass = "EnvironmentMO Class", selector = "new",
             specializer = MateNewEnvironmentPrim.IsEnvironmentMOClass.class,
             mate = true)
  public abstract static class MateNewEnvironmentPrim extends UnaryExpressionNode {
    public MateNewEnvironmentPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final DynamicObject doSClass(final DynamicObject receiver) {
      return Universe.getCurrent().getObjectMemory().newObject(receiver);
    }

    public static class IsEnvironmentMOClass extends Specializer<ExpressionNode> {
      public IsEnvironmentMOClass(final Primitive prim, final NodeFactory<ExpressionNode> fact) { super(prim, fact); }

      @Override
      public boolean matches(final Object[] args, final ExpressionNode[] argNodess) {
        try {
          return SObject.getSOMClass((DynamicObject) args[0]) == MateClasses.environmentMO;
        } catch (ClassCastException e) {
          return false;
        }
      }
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Shape Class", selector = "newWithFieldsCount:",
             eagerSpecializable = false, mate = true)
  public abstract static class MateNewShapePrim extends BinaryExpressionNode {
    public MateNewShapePrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final SAbstractObject doSClass(final DynamicObject receiver, final long fieldsCount) {
      return new SShape((int) fieldsCount);
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "changeShape:", mate = true)
  public abstract static class MateChangeShapePrim extends BinaryExpressionNode {
    public MateChangeShapePrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final DynamicObject doSObject(final DynamicObject receiver, SShape newShape) {
      receiver.setShapeAndResize(receiver.getShape(), newShape.getShape());
      return receiver;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Shape", selector = "fieldsCount",
             eagerSpecializable = false, mate = true)
  public abstract static class MateShapeFieldsCountPrim extends UnaryExpressionNode {
    public MateShapeFieldsCountPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final long doSShape(SShape shape) {
      return shape.getShape().getPropertyCount();
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "shape", mate = true)
  public abstract static class MateGetShapePrim extends UnaryExpressionNode {
    public MateGetShapePrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final SShape doSObject(DynamicObject receiver) {
      return new SShape(receiver.getShape().getPropertyCount());
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Shape", selector = "installEnvironment:",
             eagerSpecializable = false, mate = true)
  public abstract static class MateInstallEnvironmentInShapePrim extends BinaryExpressionNode {
    public MateInstallEnvironmentInShapePrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final SShape doSObject(SShape shape, DynamicObject environment) {
      return new SShape(
          shape.getShape().changeType(
              ((SReflectiveObjectType) shape.getShape().getObjectType()).setEnvironment(environment)));
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Shape", selector = "installClass:",
             eagerSpecializable = false, mate = true)
  public abstract static class MateInstallClassInShapePrim extends BinaryExpressionNode {
    public MateInstallClassInShapePrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final SShape doSObject(SShape shape, DynamicObject klass) {
      return new SShape(
          shape.getShape().changeType(
              ((SReflectiveObjectType) shape.getShape().getObjectType()).setKlass(klass)));
    }
  }


  @GenerateNodeFactory
  @Primitive(klass = "Class", selector = "updateShapeForInstancesWith:",
             eagerSpecializable = false, mate = true)
  public abstract static class MateUpdateShapeForInstancesPrim extends BinaryExpressionNode {
    public MateUpdateShapeForInstancesPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final DynamicObject doSObject(DynamicObject clazz, SShape shape) {
      // Todo: Take into account that this would not work if the factory was already compiled in a fast path.
      SClass.setInstancesFactory(clazz, shape.getShape().createFactory());
      return clazz;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Class", selector = "getShapeForInstances",
             eagerSpecializable = false, mate = true)
  public abstract static class MateGetShapeForInstancesPrim extends UnaryExpressionNode {
    public MateGetShapeForInstancesPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final SShape doSObject(DynamicObject clazz) {
      return new SShape(SClass.getFactory(clazz).getShape());
    }
  }
}
