package som.vm.constants;

import com.oracle.truffle.api.object.DynamicObject;

import som.vm.ObjectMemory;

public final class MateClasses extends Classes {
  public static final DynamicObject environmentMO;
  public static final DynamicObject operationalSemanticsMO;
  public static final DynamicObject messageMO;
  public static final DynamicObject shapeClass;
  public static final DynamicObject contextClass;
  public static final DynamicObject STANDARD_ENVIRONMENT;
  
  static {
    // Allocate the Metaclass classes
    environmentMO          = ObjectMemory.newSystemClass(objectClass, "EnvironmentMO");
    operationalSemanticsMO = ObjectMemory.newSystemClass(objectClass, "OperationalSemanticsMO");
    messageMO              = ObjectMemory.newSystemClass(objectClass, "MessageMO");
    shapeClass             = ObjectMemory.newSystemClass(objectClass, "Shape");
    contextClass           = ObjectMemory.newSystemClass(objectClass, "Context");
    STANDARD_ENVIRONMENT   = Nil.nilObject;
    //environment = Universe.newSystemClass();
  }
}