package som.vm;

import som.interpreter.Invokable;
import som.interpreter.MateifyVisitor;
import som.vmobjects.InvokableLayoutImpl;
import som.vmobjects.SBasicObjectLayoutImpl;
import som.vmobjects.SClass;
import som.vmobjects.SMateEnvironment;
import som.vmobjects.SReflectiveObject;
import som.vmobjects.SReflectiveObjectLayoutImpl;
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

  public static DynamicObject newInstance(final DynamicObject instanceClass) {
    return SReflectiveObject.create(instanceClass);
  }
  
  public static DynamicObject newEnvironment(final DynamicObject instanceClass) {
    return SMateEnvironment.create(instanceClass);
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
  
  public DynamicObject create(DynamicObject clazz){
    return SReflectiveObject.create(clazz);
  }
}