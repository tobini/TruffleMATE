package som.vm;

import static som.vm.constants.MateClasses.shapeClass;
import static som.vm.constants.MateClasses.environmentMO;
import static som.vm.constants.MateClasses.messageMO;
import static som.vm.constants.MateClasses.operationalSemanticsMO;
import static som.vm.constants.MateClasses.contextClass;
import som.interpreter.Invokable;
import som.interpreter.MateifyVisitor;
import som.interpreter.nodes.MateMessageSpecializationsFactory;
import som.interpreter.nodes.MessageSendNode.AbstractMessageSendNode;
import som.vm.constants.Nil;
import som.vmobjects.InvokableLayoutImpl;
import som.vmobjects.SBasicObjectLayoutImpl;
import som.vmobjects.SClass;
import som.vmobjects.SMateEnvironment;
import som.vmobjects.SObject;
import som.vmobjects.SReflectiveObject;
import som.vmobjects.SReflectiveObjectLayoutImpl;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class MateUniverse extends Universe {
  private Assumption mateActivated;
  private Assumption mateDeactivated;
  
  public MateUniverse() {
    super();
    mateDeactivated = this.getTruffleRuntime().createAssumption();
    mateActivated = null;
  }

  protected void initializeObjectSystem() {
    if (alreadyInitialized) {
      return;
    } else {
      super.initializeObjectSystem();
      
      //Setup the fields that were not possible to setup before to avoid cyclic initialization dependencies
      SReflectiveObject.setEnvironment(Nil.nilObject, Nil.nilObject);
      
      // Load methods and fields into the Mate MOP.
      loadSystemClass(environmentMO);
      loadSystemClass(operationalSemanticsMO);
      loadSystemClass(messageMO);
      loadSystemClass(shapeClass);
      loadSystemClass(contextClass);
      
      AbstractMessageSendNode.specializationFactory = new MateMessageSpecializationsFactory();
    }
  }
  
  public static DynamicObject newInstance(final DynamicObject instanceClass) {
    return SReflectiveObject.create(instanceClass);
  }
  
  public static DynamicObject newEnvironment(final DynamicObject instanceClass) {
    return SMateEnvironment.create(instanceClass);
  }
  
  @Override
  public DynamicObject loadClass(final SSymbol name) {
    DynamicObject result = super.loadClass(name);
    if ((DynamicObject) getGlobal(name) != null){
      try{
        mateify(result);
      } catch (NullPointerException e){
        println(name.getString());
      }
      mateify(SObject.getSOMClass(result));
    }
    return result;
  }
  
  protected void loadSystemClass(final DynamicObject systemClass) {
    super.loadSystemClass(systemClass);
    mateify(systemClass);
    mateify(SObject.getSOMClass(systemClass));
  }
  
  public void mateify(DynamicObject clazz) {
    int countOfInvokables = SClass.getNumberOfInstanceInvokables(clazz);
    MateifyVisitor visitor = new MateifyVisitor();
    for (int i = 0; i < countOfInvokables; i++){
      DynamicObject method = SClass.getInstanceInvokable(clazz, i);
      Invokable node = InvokableLayoutImpl.INSTANCE.getInvokable(method);
      node.accept(visitor);
    }
  }
  
  public void mateifyMethod(DynamicObject method) {
    MateifyVisitor visitor = new MateifyVisitor();
    Invokable node = InvokableLayoutImpl.INSTANCE.getInvokable(method);
    node.accept(visitor);
  }
  
  public static void main(final String[] arguments) {
    MateUniverse u = current();
    try {
      u.interpret(arguments);
      u.exit(0);
    } catch (IllegalStateException e) {
      errorExit(e.getMessage());
    }
  }
  
  @Override
  public DynamicObjectFactory getInstancesFactory(){
    return SReflectiveObject.SREFLECTIVE_OBJECT_FACTORY;
  }
  
  public Assumption getMateDeactivatedAssumption(){
    return this.mateDeactivated;
  }
  
  public Assumption getMateActivatedAssumption(){
    return this.mateActivated;
  }
  
  @Override
  public void activatedMate(){
    if (this.getMateDeactivatedAssumption().isValid()){
      this.getMateDeactivatedAssumption().invalidate();
    }
    this.mateActivated = this.getTruffleRuntime().createAssumption();
  }
  
  public void deactivateMate(){
    if (this.getMateActivatedAssumption().isValid()){
      this.getMateActivatedAssumption().invalidate();
    }
    this.mateDeactivated = this.getTruffleRuntime().createAssumption();
  }
  
  public static MateUniverse current() {
    if (Universe.getCurrent() == null) {
      Universe.setCurrent(new MateUniverse());
    }
    return (MateUniverse) Universe.getCurrent();
  }
  
  @Override
  public DynamicObject createNilObject() {
    DynamicObject dummyObjectForInitialization = SBasicObjectLayoutImpl.INSTANCE.createSBasicObject();
    return SReflectiveObjectLayoutImpl.INSTANCE.createSReflectiveObjectShape(dummyObjectForInitialization, dummyObjectForInitialization).newInstance();
  }
  
  @Override 
  public DynamicObjectFactory createObjectShapeFactoryForClass(final DynamicObject clazz) {
    return SReflectiveObject.createObjectShapeFactoryForClass(clazz);
  }
}