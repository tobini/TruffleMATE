package som.interpreter.nodes;

import som.interpreter.nodes.MateDispatch.MateDispatchMessageSend;
import som.vm.MateUniverse;
import som.vmobjects.SMateEnvironment;
import som.vmobjects.SReflectiveObject;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class MateNode extends ExpressionNode {
  @Child protected MateDispatch reflectiveDispatch;
  protected Object[] arguments;
  protected SMateEnvironment environment;
  
  public MateNode(final MateDispatch node, ExpressionNode wrappedNode) {
    super(node.getSourceSection());
    reflectiveDispatch = node;
  }
  
  public static MateNode createForGenericExpression(ExpressionNode node){
    MateDispatch dispatch; 
    switch (node.reflectiveOperation()){
      case Lookup:
          dispatch = MateDispatchMessageSend.create(node);
        break;
      default:
          dispatch = MateDispatch.create(node);
        break;
    }
    return MateNodeGen.create(dispatch, node);
  }
  
  @Specialization(guards="hasReflectiveBehavior(frame)")
  public Object doMetaLevel(VirtualFrame frame){
    return this.metaExecution(frame);
  }
  
  @Specialization(guards="!hasReflectiveBehavior(frame)")
  public Object doBaseLevel(VirtualFrame frame) {
    return baseExecution(frame);
  }
  
  public Object metaExecution(VirtualFrame frame){
    Object[] args = arguments.clone();
    arguments = null;
    Object value = reflectiveDispatch.executeDispatch(frame, args, environment);
    return value;
  }
  
  public Object baseExecution(VirtualFrame frame){
    Object[] args = arguments.clone();
    arguments = null;
    Object value = this.reflectiveDispatch.doBase(frame, args);
    return value;
  }
  
  protected boolean hasReflectiveBehavior(VirtualFrame frame){
    if (MateUniverse.current().executingMeta()) return false;
    if (arguments == null){
      arguments = reflectiveDispatch.evaluateArguments(frame);
    }
    Object receiver = arguments[0]; 
    //Need this check because of the possibility to receive primitive types 
    if (receiver instanceof SReflectiveObject){
      environment = ((SReflectiveObject)receiver).getEnvironment();
      if (environment != null){
        return true;
      }
      return  !((environment = ((SReflectiveObject)receiver).getEnvironment()) == null );
    } else {
      return false;
    }
  }
  
  public ExpressionNode getOriginalNode(){
    return this.getReflectiveDispatch().getBaseLevel();
  }
  
  protected MateDispatch getReflectiveDispatch(){
    return this.reflectiveDispatch;
  }
  
  public Node wrapIntoMateNode(){
    return this;
  }
}