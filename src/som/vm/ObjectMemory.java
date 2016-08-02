package som.vm;

import static som.vm.constants.Classes.arrayClass;
import static som.vm.constants.Classes.booleanClass;
import static som.vm.constants.Classes.classClass;
import static som.vm.constants.Classes.doubleClass;
import static som.vm.constants.Classes.integerClass;
import static som.vm.constants.Classes.metaclassClass;
import static som.vm.constants.Classes.methodClass;
import static som.vm.constants.Classes.nilClass;
import static som.vm.constants.Classes.objectClass;
import static som.vm.constants.Classes.primitiveClass;
import static som.vm.constants.Classes.stringClass;
import static som.vm.constants.Classes.characterClass;
import static som.vm.constants.Classes.symbolClass;
import static som.vm.constants.Classes.trueClass;
import static som.vm.constants.Classes.falseClass;
import static som.vm.constants.Classes.systemClass;
import static som.vm.constants.MateClasses.contextClass;
import static som.vm.constants.MateClasses.environmentMO;
import static som.vm.constants.MateClasses.messageMO;
import static som.vm.constants.MateClasses.operationalSemanticsMO;
import static som.vm.constants.MateClasses.shapeClass;

import java.io.IOException;
import java.util.HashMap;

import som.compiler.Disassembler;
import som.interpreter.nodes.MateMessageSpecializationsFactory;
import som.interpreter.nodes.MessageSendNode.AbstractMessageSendNode;
import som.vm.constants.Globals;
import som.vm.constants.Nil;
import som.vmobjects.SArray;
import som.vmobjects.SBlock;
import som.vmobjects.SClass;
import som.vmobjects.SObject;
import som.vmobjects.SReflectiveObject;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

//This is a pseudo object memory because the objects are actually managed by the Truffle/Java memory manager
public class ObjectMemory {
  @CompilationFinal public static ObjectMemory last;
  private final HashMap<SSymbol, DynamicObject> globals;
  private final HashMap<String, SSymbol> symbolTable;
  
  @CompilationFinal private String[] classPath;
  @CompilationFinal private DynamicObject trueObject;
  @CompilationFinal private DynamicObject falseObject;
  @CompilationFinal private DynamicObject systemObject;
  
  // Optimizations
  private final DynamicObject[] blockClasses;
  
  protected ObjectMemory(String[] path) {
    last = this;
    globals      = new HashMap<SSymbol, DynamicObject>();
    symbolTable  = new HashMap<>();
    blockClasses = new DynamicObject[5];
    classPath = path;
  }
  
  protected void initializeSystem() {
    //Setup the fields that were not possible to setup before to avoid cyclic initialization dependencies during allocation
    DynamicObject nilObject = Nil.nilObject;
    SObject.setClass(nilObject, nilClass);
    SClass.setSuperclass(SObject.getSOMClass(objectClass), classClass);
    SClass.setSuperclass(metaclassClass, classClass);
    SClass.setSuperclass(SObject.getSOMClass(metaclassClass), SObject.getSOMClass(classClass));
    
    // Load methods and fields into the system classes
    loadClass(SClass.getName(objectClass), objectClass);
    loadClass(SClass.getName(classClass), classClass);
    loadClass(SClass.getName(metaclassClass), metaclassClass);
    loadClass(SClass.getName(nilClass), nilClass);
    loadClass(SClass.getName(arrayClass), arrayClass);
    loadClass(SClass.getName(methodClass), methodClass);
    loadClass(SClass.getName(stringClass), stringClass);
    loadClass(SClass.getName(characterClass), characterClass);
    loadClass(SClass.getName(symbolClass), symbolClass);
    loadClass(SClass.getName(integerClass), integerClass);
    loadClass(SClass.getName(primitiveClass), primitiveClass);
    loadClass(SClass.getName(doubleClass), doubleClass);
    loadClass(SClass.getName(booleanClass), booleanClass);
    loadClass(SClass.getName(trueClass), trueClass);
    loadClass(SClass.getName(falseClass), falseClass);
    loadClass(SClass.getName(systemClass), systemClass);
    
    // Load the generic block class
    blockClasses[0] = loadClass(symbolFor("Block"), null);

    // Setup the true and false objects
    trueObject  = SObject.create(trueClass);
    falseObject = SObject.create(falseClass);
    // Load the system class and create an instance of it
    systemObject = SObject.create(systemClass);

    // Put special objects into the dictionary of globals
    setGlobal("nil",    nilObject);
    setGlobal("true",   trueObject);
    setGlobal("false",  falseObject);
    setGlobal("system", systemObject);

    // Load the remaining block classes
    loadBlockClass(1);
    loadBlockClass(2);
    loadBlockClass(3);
    loadBlockClass(4);
    
    if (Globals.trueObject != trueObject) {
      Universe.errorExit("Initialization went wrong for class Globals");
    }

    if (null == blockClasses[1]) {
      Universe.errorExit("Initialization went wrong for class Blocks");
    }
    
    loadClass(SClass.getName(contextClass), contextClass);

    if (Universe.getCurrent().vmReflectionEnabled()){
      //Setup the fields that were not possible to setup before to avoid cyclic initialization dependencies
      SReflectiveObject.setEnvironment(Nil.nilObject, Nil.nilObject);
      
      // Load methods and fields into the Mate MOP.
      loadClass(SClass.getName(environmentMO), environmentMO);
      loadClass(SClass.getName(operationalSemanticsMO), operationalSemanticsMO);
      loadClass(SClass.getName(messageMO), messageMO);
      loadClass(SClass.getName(shapeClass), shapeClass);
      
      AbstractMessageSendNode.specializationFactory = new MateMessageSpecializationsFactory();
    }
  }
  
  @TruffleBoundary
  public boolean hasGlobal(final SSymbol name) {
    return globals.containsKey(name);
  }
  
  @TruffleBoundary
  public DynamicObject getGlobal(final SSymbol name) {
    return globals.get(name);
  }
  
  public void setGlobal(final String name, final DynamicObject value) {
    setGlobal(symbolFor(name), value);
  }

  @TruffleBoundary
  public void setGlobal(final SSymbol name, final DynamicObject value) {
      globals.put(name, value);
  }
  
  @TruffleBoundary
  public SSymbol symbolFor(final String string) {
    String interned = string.intern();
    // Lookup the symbol in the symbol table
    SSymbol result = symbolTable.get(interned);
    if (result != null) { return result; }
    return newSymbol(interned);
  }
  
  private SSymbol newSymbol(final String string) {
    SSymbol result = new SSymbol(string);
    symbolTable.put(string, result);
    return result;
  }
  
  public static DynamicObject newSystemClass(final DynamicObject superClass, final String name) {
    DynamicObject classClassSuperClass;
    if (superClass != Nil.nilObject) {
      classClassSuperClass = SObject.getSOMClass(superClass);
    } else {
      classClassSuperClass =  Nil.nilObject;
    }
    
    DynamicObject classClass = SClass.createSClass(metaclassClass, Universe.getCurrent().symbolFor(name + " class"), classClassSuperClass, 
        SArray.create(new Object[0]), SArray.create(new Object[0]));
    return SClass.createSClass(classClass, Universe.getCurrent().symbolFor(name), superClass, SArray.create(new Object[0]), 
        SArray.create(new Object[0]));
  }
  
  public static DynamicObject newMetaclassClass(String name) {
    DynamicObject result = SClass.createWithoutClass(Universe.getCurrent().symbolFor(name));
    SObject.setClass(result, SClass.createEmptyClass(result, Universe.getCurrent().symbolFor(name + " class")));
    return result;
  }
  
  /*  If systemClass is null a new class object is created, if not the methods are loaded into systemClass.
   *  It is used mainly for system initialization.
   */
  public DynamicObject loadClass(final SSymbol name, final DynamicObject systemClass) {
    return loadClass(name,systemClass, true);
  }
  
  private DynamicObject loadClass(final SSymbol name, final DynamicObject systemClass, boolean loadPrimitives) {
    // Try loading the class from all different paths
    DynamicObject result = (DynamicObject) getGlobal(name);
    if (result != null) { return result; }
    for (String cpEntry : classPath) {
      try {
        // Load the class from a file and return the loaded class
        result = som.compiler.SourcecodeCompiler.compileClass(cpEntry,
            name.getString(), systemClass, this);
        setGlobal(name, result);
        if (loadPrimitives) loadPrimitives(result, systemClass != null);
        if (Universe.getCurrent().vmReflectionEnabled()){
          //MateUniverse.current().mateify(result);
          //MateUniverse.current().mateify(SObject.getSOMClass(result));
        }
        if (Universe.getCurrent().printAST()) {
          Disassembler.dump(SObject.getSOMClass(result));
          Disassembler.dump(result);
        }
        return result;
      } catch (IOException e) {
        // Continue trying different paths
      }
    }
    throw new IllegalStateException(name.getString()
          + " class could not be loaded. "
          + "It is likely that the class path has not been initialized properly. "
          + "Please set system property 'system.class.path' or "
          + "pass the '-cp' command-line parameter.");
  }

  private void loadPrimitives(final DynamicObject result, final boolean isSystemClass) {
    if (SClass.hasPrimitives(result) || isSystemClass) {
      SClass.loadPrimitives(result, !isSystemClass);
    }
  }
  
  private void loadBlockClass(final int numberOfArguments) {
    // Compute the name of the block class with the given number of
    // arguments
    SSymbol name = symbolFor("Block" + numberOfArguments);
    assert getGlobal(name) == null;

    // Get the block class for blocks with the given number of arguments
    DynamicObject result = loadClass(name, null, false);

    // Add the appropriate value primitive to the block class
    SClass.addInstancePrimitive(result, SBlock.getEvaluationPrimitive(
        numberOfArguments, Universe.getCurrent(), result), true);

    // Insert the block class into the dictionary of globals
    setGlobal(name, result);

    blockClasses[numberOfArguments] = result;
  }
  
  @TruffleBoundary
  public DynamicObject loadShellClass(final String stmt) throws IOException {
    // Load the class from a stream and return the loaded class
    DynamicObject result = som.compiler.SourcecodeCompiler.compileClass(stmt, null, this);
    if (Universe.getCurrent().printAST()) { Disassembler.dump(result); }
    return result;
  }
  
  public DynamicObject getBlockClass(final int numberOfArguments) {
    DynamicObject result = blockClasses[numberOfArguments];
    assert result != null || numberOfArguments == 0;
    return result;
  }
  
  public void setClassPath(String[] path){
    classPath = path;
  }
  
  public DynamicObject getTrueObject()   { return trueObject; }
  public DynamicObject getFalseObject()  { return falseObject; }
  public DynamicObject getTrueClass()   { return trueClass; }
  public DynamicObject getFalseClass()  { return falseClass; }
  public DynamicObject getSystemObject() { return systemObject; }
  public DynamicObject getSystemClass() { return systemClass; }
}
