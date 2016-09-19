package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SMateEnvironment;
import som.vmobjects.SReflectiveObjectLayoutImpl.SReflectiveObjectType;
import som.vmobjects.SShape;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public final class MatePrims {
  @GenerateNodeFactory
  public abstract static class MateNewEnvironmentPrim extends UnaryExpressionNode {
    public MateNewEnvironmentPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "New Environment"));
    }

    @Specialization
    public final DynamicObject doSClass(final DynamicObject receiver) {
      return SMateEnvironment.create(receiver);
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateNewShapePrim extends BinaryExpressionNode {
    public MateNewShapePrim() {
      super(Source.newBuilder("New Shape").internal().name("mate new shape").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final SAbstractObject doSClass(final DynamicObject receiver, final long fieldsCount) {
      return new SShape((int)fieldsCount);
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateChangeShapePrim extends BinaryExpressionNode {
    public MateChangeShapePrim() {
      super(Source.newBuilder("Change Shape").internal().name("mate change shape").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final DynamicObject doSObject(final DynamicObject receiver, SShape newShape) {
      receiver.setShapeAndResize(receiver.getShape(), newShape.getShape());
      return receiver;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateShapeFieldsCountPrim extends UnaryExpressionNode {
    public MateShapeFieldsCountPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Shape Fields"));
    }

    @Specialization
    public final long doSShape(SShape shape) {
      return shape.getShape().getPropertyCount();
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateGetShapePrim extends UnaryExpressionNode {
    public MateGetShapePrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Get Shape"));
    }

    @Specialization
    public final SShape doSObject(DynamicObject receiver) {
      return new SShape(receiver.getShape().getPropertyCount());
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateInstallEnvironmentInShapePrim extends BinaryExpressionNode {
    public MateInstallEnvironmentInShapePrim() {
      super(Source.newBuilder("Install Environment in Shape").internal().name("mate install env in shape").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final SShape doSObject(SShape shape, DynamicObject environment) {
      return new SShape(
          shape.getShape().changeType(
              ((SReflectiveObjectType)shape.getShape().getObjectType()).setEnvironment(environment)));
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateUpdateShapeForInstancesPrim extends BinaryExpressionNode {
    public MateUpdateShapeForInstancesPrim() {
      super(Source.newBuilder("Update Class Instances Shape").internal().name("update shape for instances").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public final DynamicObject doSObject(DynamicObject clazz, SShape shape) {
      //Todo: Take into account that this would not work if the factory was already compiled in a fast path.
      SClass.setInstancesFactory(clazz, shape.getShape().createFactory());
      return clazz;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class MateGetShapeForInstancesPrim extends UnaryExpressionNode {
    public MateGetShapeForInstancesPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Get Shape For Instances"));
    }

    @Specialization
    public final SShape doSObject(DynamicObject clazz) {
      return new SShape(SClass.getFactory(clazz).getShape());
    }
  }
}

