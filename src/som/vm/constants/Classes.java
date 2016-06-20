package som.vm.constants;

import som.vm.Universe;

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
  public static final DynamicObject  doubleClass;

  public static final DynamicObject  booleanClass;
  
  // These classes can be statically preinitialized.
  static {
    // Allocate the Metaclass classes
    metaclassClass = Universe.newMetaclassClass("Metaclass");
    // Allocate the rest of the system classes
    objectClass     = Universe.newSystemClass(Nil.nilObject, "Object");
    nilClass        = Universe.newSystemClass(objectClass, "Nil");
    classClass      = Universe.newSystemClass(objectClass, "Class");
    arrayClass      = Universe.newSystemClass(objectClass, "Array");
    stringClass     = Universe.newSystemClass(objectClass, "String");
    symbolClass     = Universe.newSystemClass(stringClass, "Symbol");
    methodClass     = Universe.newSystemClass(objectClass, "Method");
    integerClass    = Universe.newSystemClass(objectClass, "Integer");
    primitiveClass  = Universe.newSystemClass(objectClass, "Primitive");
    doubleClass     = Universe.newSystemClass(objectClass, "Double");
    booleanClass    = Universe.newSystemClass(objectClass, "Boolean");
  }
}
