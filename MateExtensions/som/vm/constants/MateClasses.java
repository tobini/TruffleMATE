package som.vm.constants;

import com.oracle.truffle.api.object.DynamicObject;

import som.vm.MateUniverse;
import som.vm.Universe;

public final class MateClasses extends Classes {
  public static final DynamicObject environmentMO;
  public static final DynamicObject operationalSemanticsMO;
  public static final DynamicObject messageMO;
  public static final DynamicObject shapeClass;
  public static final DynamicObject contextClass;
  public static final DynamicObject STANDARD_ENVIRONMENT;
  
  static {
    // Allocate the Metaclass classes
    environmentMO = MateUniverse.newSystemClass(objectClass, "EnvironmentMO");
    operationalSemanticsMO = MateUniverse.newSystemClass(objectClass, "OperationalSemanticsMO");
    messageMO = MateUniverse.newSystemClass(objectClass, "MessageMO");
    shapeClass = MateUniverse.newSystemClass(objectClass, "Shape");
    contextClass    = Universe.newSystemClass(objectClass, "Context");
    STANDARD_ENVIRONMENT = Nil.nilObject;
    //environment = Universe.newSystemClass();
  }
}