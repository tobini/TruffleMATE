package som.primitives;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SMateEnvironment;
import som.vmobjects.SShape;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public final class MatePrims {
  @GenerateNodeFactory
  public abstract static class MateNewEnvironmentPrim extends UnaryExpressionNode {
    @Specialization
    public final DynamicObject doSClass(final DynamicObject receiver) {
      return SMateEnvironment.create(receiver);
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateNewShapePrim extends BinaryExpressionNode {
    @Specialization
    public final SAbstractObject doSClass(final DynamicObject receiver, final long fieldsCount) {
      return new SShape((int)fieldsCount);
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateChangeShapePrim extends BinaryExpressionNode {
    @Specialization
    public final DynamicObject doSObject(final DynamicObject receiver, SShape newShape) {
      receiver.setShapeAndResize(receiver.getShape(), newShape.getShape());
      return receiver;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateShapeFieldsCountPrim extends UnaryExpressionNode {
    @Specialization
    public final long doSShape(SShape shape) {
      return shape.getShape().getPropertyCount();
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateGetShapePrim extends UnaryExpressionNode {
    @Specialization
    public final SShape doSObject(DynamicObject receiver) {
      return new SShape(receiver.getShape().getPropertyCount());
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateInstallEnvironmentInShapePrim extends BinaryExpressionNode {
    @Specialization
    public final SShape doSObject(SShape shape, DynamicObject environment) {
      //Check what to do in this version where the environment is in the objects instead of in the shapes.
      return null;
      /*return new SShape(
          shape.getShape().changeType(
              ((SReflectiveObjectType)shape.getShape().getObjectType()).setEnvironment(environment)));*/
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateUpdateShapeForInstancesPrim extends BinaryExpressionNode {
    @Specialization
    public final DynamicObject doSObject(DynamicObject clazz, SShape shape) {
      //Todo: Take into account that this would not work if the factory was already compiled in a fast path.
      SClass.setInstancesFactory(clazz, shape.getShape().createFactory());
      return clazz;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateGetShapeForInstancesPrim extends UnaryExpressionNode {
    @Specialization
    public final SShape doSObject(DynamicObject clazz) {
      return new SShape(SClass.getFactory(clazz).getShape());
    }
  }
}

