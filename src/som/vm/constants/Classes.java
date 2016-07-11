package som.vm.constants;

import som.vm.ObjectMemory;
import com.oracle.truffle.api.object.DynamicObject;

public class Classes {
  public static final DynamicObject  objectClass;
  public static final DynamicObject  classClass;
  public static final DynamicObject  metaclassClass;
  public static final DynamicObject  nilClass;
  public static final DynamicObject  integerClass;
  public static final DynamicObject  arrayClass;
  public static final DynamicObject  methodClass;
  public static final DynamicObject  symbolClass;
  public static final DynamicObject  primitiveClass;
  public static final DynamicObject  stringClass;
  public static final DynamicObject  characterClass;
  public static final DynamicObject  doubleClass;
  public static final DynamicObject  booleanClass;
  public static final DynamicObject contextClass;
  public static final DynamicObject  trueClass;
  public static final DynamicObject  falseClass;
  public static final DynamicObject  systemClass;
  // These classes can be statically preinitialized.
  
  static {
    // Allocate the Metaclass classes
    metaclassClass = ObjectMemory.newMetaclassClass("Metaclass");
    // Allocate the rest of the system classes
    objectClass     = ObjectMemory.newSystemClass(Nil.nilObject, "Object");
    nilClass        = ObjectMemory.newSystemClass(objectClass, "Nil");
    classClass      = ObjectMemory.newSystemClass(objectClass, "Class");
    arrayClass      = ObjectMemory.newSystemClass(objectClass, "Array");
    stringClass     = ObjectMemory.newSystemClass(objectClass, "String");
    characterClass  = ObjectMemory.newSystemClass(objectClass, "Character");
    symbolClass     = ObjectMemory.newSystemClass(stringClass, "Symbol");
    methodClass     = ObjectMemory.newSystemClass(objectClass, "Method");
    integerClass    = ObjectMemory.newSystemClass(objectClass, "Integer");
    primitiveClass  = ObjectMemory.newSystemClass(objectClass, "Primitive");
    doubleClass     = ObjectMemory.newSystemClass(objectClass, "Double");
    booleanClass    = ObjectMemory.newSystemClass(objectClass, "Boolean");
    trueClass       = ObjectMemory.newSystemClass(booleanClass, "True");
    falseClass      = ObjectMemory.newSystemClass(booleanClass, "False");
    systemClass     = ObjectMemory.newSystemClass(objectClass, "System");
    contextClass    = ObjectMemory.newSystemClass(objectClass, "Context");
  }
}
