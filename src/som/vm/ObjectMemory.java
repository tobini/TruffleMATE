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
import tools.language.StructuralProbe;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

//This is a pseudo object memory because the objects are actually managed by the Truffle/Java memory manager
public class ObjectMemory {
  @CompilationFinal public static ObjectMemory last;
  private final HashMap<SSymbol, DynamicObject> globals;
  private final HashMap<String, SSymbol> symbolTable;
  
  @CompilationFinal private DynamicObject trueObject;
  @CompilationFinal private DynamicObject falseObject;
  @CompilationFinal private DynamicObject systemObject;
  
  // Optimizations
  private final DynamicObject[] blockClasses;
  private final StructuralProbe structuralProbe;
  
  protected ObjectMemory(final String[] path, final StructuralProbe probe) {
    last = this;
    globals      = new HashMap<SSymbol, DynamicObject>();
    symbolTable  = new HashMap<>();
    blockClasses = new DynamicObject[5];
    structuralProbe = probe;
  }
  
  protected void initializeSystem() {
    //Setup the fields that were not possible to setup before to avoid cyclic initialization dependencies during allocation
    DynamicObject nilObject = Nil.nilObject;
    SObject.setClass(nilObject, nilClass);
    SClass.setSuperclass(SObject.getSOMClass(objectClass), classClass);
    SClass.setSuperclass(metaclassClass, classClass);
    SClass.setSuperclass(SObject.getSOMClass(metaclassClass), SObject.getSOMClass(classClass));
    
    // Load methods and fields into the system classes
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(objectClass)), objectClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(classClass)), classClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(metaclassClass)), metaclassClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(nilClass)), nilClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(arrayClass)), arrayClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(methodClass)), methodClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(stringClass)), stringClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(characterClass)), characterClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(symbolClass)), symbolClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(integerClass)), integerClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(primitiveClass)), primitiveClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(doubleClass)), doubleClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(booleanClass)), booleanClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(trueClass)), trueClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(falseClass)), falseClass, true);
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(systemClass)), systemClass, true);
    
    // Load the generic block class
    blockClasses[0] = loadClass(Universe.getCurrent().getSourceForClassName(symbolFor("Block")), null, true);

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
    
    loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(contextClass)), contextClass, true);

    if (Universe.getCurrent().vmReflectionEnabled()){
      //Setup the fields that were not possible to setup before to avoid cyclic initialization dependencies
      SReflectiveObject.setEnvironment(Nil.nilObject, Nil.nilObject);
      
      // Load methods and fields into the Mate MOP.
      loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(environmentMO)), environmentMO, true);
      loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(operationalSemanticsMO)), operationalSemanticsMO, true);
      loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(messageMO)), messageMO, true);
      loadClass(Universe.getCurrent().getSourceForClassName(SClass.getName(shapeClass)), shapeClass, true);
      
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
  
  /*  
   *  If systemClass is null a new class object is created, if not the methods are loaded into systemClass.
   *  Used mainly for system initialization.
   */
  public DynamicObject loadClass(final Source source, final DynamicObject systemClass, boolean loadPrimitives) {
    // Try loading the class from all different paths
    // Load the class from a file and return the loaded class
    DynamicObject result = som.compiler.SourcecodeCompiler.compileClass(source,
        systemClass, this, structuralProbe);
    setGlobal(source.getName(), result);
    if (loadPrimitives) loadPrimitives(result, systemClass != null);
    if (Universe.getCurrent().vmReflectionEnabled()){
      Universe.getCurrent().mateify(result);
      Universe.getCurrent().mateify(SObject.getSOMClass(result));
    }
    if (Universe.getCurrent().printAST()) {
      Disassembler.dump(SObject.getSOMClass(result));
      Disassembler.dump(result);
    }
    return result;
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
    DynamicObject result = loadClass(Universe.getCurrent().getSourceForClassName(name), null, false);

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
    DynamicObject result = som.compiler.SourcecodeCompiler.compileClass(stmt, null, this, structuralProbe);
    if (Universe.getCurrent().printAST()) { Disassembler.dump(result); }
    return result;
  }
  
  public DynamicObject getBlockClass(final int numberOfArguments) {
    DynamicObject result = blockClasses[numberOfArguments];
    assert result != null || numberOfArguments == 0;
    return result;
  }
  
  public DynamicObject getTrueObject()   { return trueObject; }
  public DynamicObject getFalseObject()  { return falseObject; }
  public DynamicObject getTrueClass()   { return trueClass; }
  public DynamicObject getFalseClass()  { return falseClass; }
  public DynamicObject getSystemObject() { return systemObject; }
  public DynamicObject getSystemClass() { return systemClass; }
}
