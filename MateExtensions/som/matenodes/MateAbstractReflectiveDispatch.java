package som.matenodes;

import som.interpreter.SArguments;
import som.interpreter.Types;
import som.interpreter.nodes.ISuperReadNode;
import som.interpreter.nodes.MateMethodActivationNode;
import som.vm.MateUniverse;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SArray;
import som.vmobjects.SInvokable;
import som.vmobjects.SObject;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;

public abstract class MateAbstractReflectiveDispatch extends Node {

  protected final static int INLINE_CACHE_SIZE = 6;
  
  public MateAbstractReflectiveDispatch(final SourceSection source) {
    //super(source);
    super();
  }

  protected Object[] computeArgumentsForMetaDispatch(VirtualFrame frame, Object[] arguments) {
    return SArguments.createSArguments(SArguments.getEnvironment(frame), ExecutionLevel.Meta, arguments);
  }

  public DirectCallNode createDispatch(final DynamicObject metaMethod) {
    DirectCallNode node = MateUniverse.current().getTruffleRuntime()
        .createDirectCallNode(SInvokable.getCallTarget(metaMethod));
    node.forceInlining();
    return node;
  }

  public abstract static class MateDispatchFieldAccessor extends
      MateAbstractReflectiveDispatch {

    public MateDispatchFieldAccessor(final SourceSection source) {
      super(source);
    }
  }
  
  @Override
  public NodeCost getCost() {
    return NodeCost.NONE;
  }
  
  public abstract static class MateAbstractStandardDispatch extends
      MateAbstractReflectiveDispatch {

    public MateAbstractStandardDispatch(SourceSection source) {
      super(source);
    }

    public abstract Object executeDispatch(final VirtualFrame frame,
        DynamicObject method, Object subject, Object[] arguments);
  }

  public abstract static class MateDispatchFieldRead extends
      MateAbstractStandardDispatch {

    public MateDispatchFieldRead(SourceSection source) {
      super(source);
    }
    
    @Specialization(guards = "cachedMethod==method", limit = "INLINE_CACHE_SIZE")
    public Object doMateNode(final VirtualFrame frame, final DynamicObject method,
        final Object subject, final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("createDispatch(method)") final DirectCallNode reflectiveMethod) {
      return reflectiveMethod.call(frame, this.computeArgumentsForMetaDispatch(frame, arguments));
    }
    
    @Specialization(contains = {"doMateNode"})
    public Object doMegaMorphic(final VirtualFrame frame, final DynamicObject method,
        final Object subject, final Object[] arguments,
        @Cached("createIndirectCall()") final IndirectCallNode callNode) {
      return callNode.call(frame, SInvokable.getCallTarget(method), this.computeArgumentsForMetaDispatch(frame, arguments));
    }
    
    @Override
    protected Object[] computeArgumentsForMetaDispatch(VirtualFrame frame, Object[] arguments) {
      return new Object[]{SArguments.getEnvironment(frame), ExecutionLevel.Meta, arguments[0], arguments[1]};
    }
  }
  
  public abstract static class MateDispatchFieldWrite extends
      MateDispatchFieldRead {
  
    public MateDispatchFieldWrite(SourceSection source) {
      super(source);
    }
    
    @Override
    protected Object[] computeArgumentsForMetaDispatch(VirtualFrame frame, Object[] arguments) {
      return new Object[]{SArguments.getEnvironment(frame), ExecutionLevel.Meta, arguments[0], arguments[1], arguments[2]};
    }
  }

  public abstract static class MateDispatchMessageLookup extends
      MateAbstractStandardDispatch {

    private final SSymbol    selector;
    @Child MateMethodActivationNode activationNode;

    public MateDispatchMessageLookup(SourceSection source, SSymbol sel) {
      super(source);
      selector = sel;
      activationNode = new MateMethodActivationNode();
    }

    @Specialization(guards = {"cachedMethod==method"})
    public Object doMateNode(final VirtualFrame frame, final DynamicObject method,
        final DynamicObject subject, final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("createDispatch(method)") final DirectCallNode reflectiveMethod) {
      // The MOP receives the class where the lookup must start (find: aSelector since: aClass)
      DynamicObject actualMethod = this.reflectiveLookup(frame, reflectiveMethod, subject, lookupSinceFor(subject));
      return activationNode.doActivation(frame, actualMethod, arguments);
    }
    
    @Specialization(guards = {"cachedMethod==method"})
    public Object doMatePrimitiveNode(final VirtualFrame frame, final DynamicObject method,
        final Object subject, final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("createDispatch(method)") final DirectCallNode reflectiveMethod){
      // The MOP receives the class where the lookup must start (find: aSelector since: aClass)
      DynamicObject actualMethod = this.reflectiveLookup(frame, reflectiveMethod, subject, lookupSinceFor(subject));
      return activationNode.doActivation(frame, actualMethod, arguments);
    }
    
    public DynamicObject reflectiveLookup(final VirtualFrame frame, DirectCallNode reflectiveMethod,
        final Object receiver, DynamicObject lookupSince) {
      Object[] args = { SArguments.getEnvironment(frame), ExecutionLevel.Meta, receiver, this.getSelector(), lookupSince};
      return (DynamicObject) reflectiveMethod.call(frame, args);
    }
    
    protected DynamicObject classOfReceiver(DynamicObject receiver){
      return SObject.getSOMClass(receiver);
    }
    
    protected DynamicObject classOfReceiver(Object receiver){
      return Types.getClassOf(receiver);
    }
    
    protected DynamicObject lookupSinceFor(DynamicObject receiver){
      return classOfReceiver(receiver);
    }
    
    protected DynamicObject lookupSinceFor(Object receiver){
      return classOfReceiver(receiver);
    }
    
    protected SSymbol getSelector() {
      return selector;
    }    
  }
  
  public abstract static class MateDispatchSuperMessageLookup extends MateDispatchMessageLookup{
    ISuperReadNode superNode;
    
    public MateDispatchSuperMessageLookup(SourceSection source, SSymbol sel, ISuperReadNode node) {
      super(source, sel);
      superNode = node;
    }

    @Override
    protected DynamicObject lookupSinceFor(DynamicObject receiver){
      return superNode.getLexicalSuperClass();
    }
    
    @Override
    protected DynamicObject lookupSinceFor(Object receiver){
      return superNode.getLexicalSuperClass();
    }
  }
  
  public abstract static class MateCachedDispatchMessageLookup extends
    MateDispatchMessageLookup {

    public MateCachedDispatchMessageLookup(SourceSection source, SSymbol sel) {
      super(source, sel);
    }
    
    @Specialization(guards = {"cachedMethod==method", "classOfReceiver(subject) == cachedClass"}, 
        insertBefore="doMateNode", limit = "3")
    public Object doMatePrimitiveNodeCached(final VirtualFrame frame, final DynamicObject method,
        final Object subject, final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("classOfReceiver(subject)") final DynamicObject cachedClass,
        @Cached("lookupResult(frame, method, subject, arguments)") final DynamicObject lookupResult){
      // The MOP receives the class where the lookup must start (find: aSelector since: aClass)
      return activationNode.doActivation(frame, lookupResult, arguments);
    }

    @Specialization(guards = {"cachedMethod==method", "shapeOfReceiver(arguments) == cachedShape"}, 
        insertBefore="doMateNode", limit = "INLINE_CACHE_SIZE")
    public Object doMateNodeCached(final VirtualFrame frame, final DynamicObject method,
        final DynamicObject subject, final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("shapeOfReceiver(arguments)") final Shape cachedShape,
        @Cached("lookupResult(frame, method, subject, arguments)") final DynamicObject lookupResult){
      // The MOP receives the class where the lookup must start (find: aSelector since: aClass)
      return activationNode.doActivation(frame, lookupResult, arguments);
    }
    
    @Specialization(guards = {"cachedMethod==method"}, contains = {"doMateNodeCached"}, insertBefore="doMateNode")
    public Object doMegaMorphic(final VirtualFrame frame, final DynamicObject method,
        final DynamicObject subject, final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("createDispatch(method)") final DirectCallNode reflectiveMethod) {
      return super.doMateNode(frame, method, subject, arguments, cachedMethod, reflectiveMethod);
    }
    
    protected Shape shapeOfReceiver(Object[] arguments){
      return ((DynamicObject) arguments[0]).getShape();
    }
    
    public DynamicObject lookupResult(final VirtualFrame frame, final DynamicObject method,
        final Object receiver, final Object[] arguments){
      if (receiver instanceof DynamicObject){
        return this.reflectiveLookup(frame, this.createDispatch(method), receiver, lookupSinceFor((DynamicObject)receiver));
      } else {
        return this.reflectiveLookup(frame, this.createDispatch(method), receiver, lookupSinceFor(receiver));
      }
    }
  }
  
  public abstract static class MateCachedDispatchSuperMessageLookup extends MateCachedDispatchMessageLookup{
    ISuperReadNode superNode;
    
    public MateCachedDispatchSuperMessageLookup(SourceSection source, SSymbol sel, ISuperReadNode node) {
      super(source, sel);
      superNode = node;
    }

    @Override
    protected DynamicObject lookupSinceFor(DynamicObject receiver){
      return superNode.getLexicalSuperClass();
    }
  }
  
  public abstract static class MateActivationDispatch extends
      MateAbstractReflectiveDispatch {

    public MateActivationDispatch(SourceSection source) {
      super(source);
    }

    public abstract Object executeDispatch(final VirtualFrame frame,
        DynamicObject method, DynamicObject methodToActivate, Object[] arguments);

    @Specialization(guards = {"cachedMethod==method","methodToActivate == cachedMethodToActivate"}, limit = "INLINE_CACHE_SIZE")
    public Object doMetaLevel(final VirtualFrame frame, 
        final DynamicObject method, final DynamicObject methodToActivate,
        final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("methodToActivate") final DynamicObject cachedMethodToActivate,
        @Cached("createDirectCall(methodToActivate)") final DirectCallNode callNode,
        @Cached("createDispatch(method)") final DirectCallNode reflectiveMethod) {
      // The MOP receives the standard ST message Send stack (rcvr, method, arguments) and returns its own
      Object[] args = { SArguments.getEnvironment(frame), ExecutionLevel.Meta, arguments[0], methodToActivate, 
          SArray.create(SArguments.createSArguments(SArguments.getEnvironment(frame), ExecutionLevel.Base, arguments))};
      SArray realArguments = (SArray)reflectiveMethod.call(frame, args);
      return callNode.call(frame, realArguments.toJavaArray());
    }
    
    @Specialization(guards = {"cachedMethod==method"}, contains = "doMetaLevel")
    public Object doMegamorphicMetaLevel(final VirtualFrame frame,
        final DynamicObject method, final DynamicObject methodToActivate,
        final Object[] arguments,
        @Cached("method") final DynamicObject cachedMethod,
        @Cached("createDispatch(method)") final DirectCallNode reflectiveMethod,
        @Cached("createIndirectCall()") final IndirectCallNode callNode){
      Object[] args = { SArguments.getEnvironment(frame), ExecutionLevel.Meta, (DynamicObject) arguments[0], methodToActivate, 
          SArray.create(SArguments.createSArguments(SArguments.getEnvironment(frame), ExecutionLevel.Base, arguments))};
      SArray realArguments = (SArray)reflectiveMethod.call(frame, args);
      return callNode.call(frame, SInvokable.getCallTarget(methodToActivate), realArguments.toJavaArray());
    }
  }
  
  public static DirectCallNode createDirectCall(DynamicObject methodToActivate){
    DirectCallNode node = DirectCallNode.create(SInvokable.getCallTarget(methodToActivate)); 
    node.forceInlining();
    return node;
  }
  
  public static IndirectCallNode createIndirectCall(){
    return IndirectCallNode.create();
  }
}
