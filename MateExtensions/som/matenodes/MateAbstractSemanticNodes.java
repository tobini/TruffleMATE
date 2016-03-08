package som.matenodes;

import som.interpreter.SArguments;
import som.matenodes.MateAbstractSemanticNodesFactory.MateEnvironmentSemanticCheckNodeGen;
import som.matenodes.MateAbstractSemanticNodesFactory.MateObjectSemanticCheckNodeGen;
import som.matenodes.MateAbstractSemanticNodesFactory.MateSemanticCheckNodeGen;
import som.vm.MateUniverse;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.Nil;
import som.vm.constants.ReflectiveOp;
import som.vmobjects.SInvokable;
import som.vmobjects.SMateEnvironment;
import som.vmobjects.SReflectiveObject;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;

public abstract class MateAbstractSemanticNodes {

  public static abstract class MateEnvironmentSemanticCheckNode extends Node {
    private final ReflectiveOp reflectiveOperation;

    protected MateEnvironmentSemanticCheckNode(ReflectiveOp operation) {
      super();
      reflectiveOperation = operation;
    }

    public abstract SInvokable executeGeneric(VirtualFrame frame);

    
    @Specialization(guards = "getEnvironment(frame) == null")
    public SInvokable doNoSemanticsInFrame(final VirtualFrame frame) {
      return null;
    }
    
    @Specialization(guards = {"getEnvironment(frame) == environment"})
    public SInvokable doSemanticsInFrame(final VirtualFrame frame,
        @Cached("getEnvironment(frame)") final DynamicObject environment,
        @Cached("methodImplementingOperationOn(environment)") final SInvokable reflectiveMethod) {
        return reflectiveMethod;
    }
    
    public static DynamicObject getEnvironment(VirtualFrame frame){
      return SArguments.getEnvironment(frame);
    }
    
    public SInvokable methodImplementingOperationOn(final DynamicObject environment){
      return SMateEnvironment.methodImplementing(environment, this.reflectiveOperation);
    }
    
    @Override
    public NodeCost getCost() {
      return NodeCost.NONE;
    }
    
    public ReflectiveOp getReflectiveOperation(){
      return this.reflectiveOperation;
    }
  }

  public static abstract class MateObjectSemanticCheckNode extends Node {

    protected final ReflectiveOp reflectiveOperation;

    protected MateObjectSemanticCheckNode(ReflectiveOp operation) {
      super();
      reflectiveOperation = operation;
    }

    public abstract SInvokable executeGeneric(VirtualFrame frame,
        Object receiver);

    @Specialization(guards = "!isSReflectiveObject(receiver)")
    public SInvokable doStandardSOMForPrimitives(final VirtualFrame frame,
        final DynamicObject receiver) {
      return null;
    }
    
    @Specialization(guards = {"receiver.getShape().getObjectType() == cachedType"}, limit = "10")
    public SInvokable doSReflectiveObject(
        final VirtualFrame frame,
        final DynamicObject receiver,
        @Cached("receiver.getShape()") final Shape cachedShape,
        @Cached("cachedShape.getObjectType()") final ObjectType cachedType,
        @Cached("getEnvironment(cachedShape)") final DynamicObject cachedEnvironment,
        @Cached("environmentReflectiveMethod(cachedEnvironment, reflectiveOperation)") final SInvokable method) {
      return method;
    }
    
    @Specialization(contains={"doSReflectiveObject", "doStandardSOMForPrimitives"})
    public SInvokable doMegamorphicReceiver(
        final VirtualFrame frame,
        final DynamicObject receiver) {
      return environmentReflectiveMethod(SReflectiveObject.getEnvironment(receiver), this.reflectiveOperation);
    }
    
    protected static SInvokable environmentReflectiveMethod(
        DynamicObject environment, ReflectiveOp operation) {
      if (environment == Nil.nilObject){
        return null;
      }
      return SMateEnvironment.methodImplementing(environment, operation);
    }
    
    public static DynamicObject getEnvironment(Shape shape){
      return SReflectiveObject.getEnvironment(shape);
    }
    
    public static boolean isSReflectiveObject(DynamicObject object){
      /*return 
           SReflectiveObject.isSReflectiveObject(object) | 
           SObject.isSObject(object) |
           SClass.isSClass(object);*/
      return SReflectiveObject.isSReflectiveObject(object);
    }
    
    @Override
    public NodeCost getCost() {
      return NodeCost.NONE;
    }
    
    public ReflectiveOp getReflectiveOperation(){
      return this.reflectiveOperation;
    }
  }

  public static abstract class MateSemanticCheckNode extends Node {
    @Child MateEnvironmentSemanticCheckNode environment;
    @Child MateObjectSemanticCheckNode      object;

    public abstract SInvokable execute(final VirtualFrame frame,
        Object[] arguments);

    public MateSemanticCheckNode(MateEnvironmentSemanticCheckNode env,
        MateObjectSemanticCheckNode obj) {
      super();
      environment = env;
      object = obj;
    }

    public static MateSemanticCheckNode createForFullCheck(
        SourceSection source, ReflectiveOp operation) {
      return MateSemanticCheckNodeGen.create(
          MateEnvironmentSemanticCheckNodeGen.create(operation),
          MateObjectSemanticCheckNodeGen.create(operation));
    }

    @Specialization
    protected SInvokable executeGeneric(final VirtualFrame frame,
        Object[] arguments) {
      if (SArguments.getExecutionLevel(frame) == ExecutionLevel.Base & arguments[0] instanceof DynamicObject){
        DynamicObject env = SArguments.getEnvironment(frame);
        SInvokable method = null;
        if (env != null){
           method = SMateEnvironment.methodImplementing(env, environment.getReflectiveOperation());
        }
        if (method == null & SReflectiveObject.isSReflectiveObject(((DynamicObject)arguments[0]))){  
          env = SReflectiveObject.getEnvironment(((DynamicObject)arguments[0]).getShape());
          if (env != Nil.nilObject){
            method = SMateEnvironment.methodImplementing(env, object.getReflectiveOperation());
          }  
        }
        return method;
      }
      return null;
    }

    public MateSemanticCheckNode(final SourceSection source,
        ReflectiveOp operation) {
      super(source);
      environment = MateEnvironmentSemanticCheckNodeGen.create(operation);
      object = MateObjectSemanticCheckNodeGen.create(operation);
    }

    public static boolean executeBase(VirtualFrame frame) {
      return SArguments.getExecutionLevel(frame) == ExecutionLevel.Base;
    }
    
    public static Assumption getMateDeactivatedAssumption() {
      return MateUniverse.current().getMateDeactivatedAssumption();
    }
    
    public static Assumption getMateActivatedAssumption() {
      return MateUniverse.current().getMateActivatedAssumption();
    }
    
    @Override
    public NodeCost getCost() {
      return NodeCost.NONE;
    }
  }
}
