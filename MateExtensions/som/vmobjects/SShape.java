package som.vmobjects;

import som.vm.constants.MateClasses;
import som.vm.constants.Nil;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

public class SShape extends SAbstractObject {

  private Shape mockShape;

  @Override
  public DynamicObject getSOMClass() {
    return MateClasses.shapeClass;
  }

  public SShape(int fieldsCount) {
    Shape newShape = SReflectiveObjectLayoutImpl.INSTANCE.createSReflectiveObjectShape(Nil.nilObject, Nil.nilObject).getShape();
    for (int i = 0; i < fieldsCount; i++) {
      newShape = newShape.defineProperty(i, Nil.nilObject, 0);
    }
    mockShape = newShape;
  }

  public SShape(Shape shape) {
    mockShape = shape;
  }

  public Shape getShape() {
    return mockShape;
  }

  @Override
  public ForeignAccess getForeignAccess() {
    // TODO Auto-generated method stub
    return null;
  }
}
