package som.interpreter.nodes;

import som.interpreter.SArguments;
import som.interpreter.nodes.dispatch.AbstractMethodDispatchNode;
import som.interpreter.nodes.dispatch.GenericMethodDispatchNode;
import som.interpreter.nodes.dispatch.UninitializedMethodDispatchNode;
import som.matenodes.MateAbstractReflectiveDispatch.MateActivationDispatch;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateActivationDispatchNodeGen;
import som.matenodes.MateAbstractSemanticNodes.MateAbstractSemanticsLevelNode;
import som.matenodes.MateAbstractSemanticNodes.MateSemanticCheckNode;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.ReflectiveOp;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;


public class MateMethodActivationNode extends Node {
  @Child MateAbstractSemanticsLevelNode  semanticCheck;
  @Child MateActivationDispatch reflectiveDispatch;
  @Child AbstractMethodDispatchNode methodDispatch;
  private final ConditionProfile semanticsRedefined = ConditionProfile.createBinaryProfile();
  
  public MateMethodActivationNode(){
    semanticCheck = MateSemanticCheckNode.createForFullCheck(this.getSourceSection(), ReflectiveOp.MessageActivation);
    reflectiveDispatch = MateActivationDispatchNodeGen.create();
    methodDispatch = new UninitializedMethodDispatchNode();
  }
  
  public Object doActivation(final VirtualFrame frame, DynamicObject method, Object[] arguments) {
    DynamicObject mateMethod = this.getMateNode().execute(frame, arguments);
    if (semanticsRedefined.profile(mateMethod != null)){
      return this.getMateDispatch().executeDispatch(frame, mateMethod, method, arguments);
    } else {
      return methodDispatch.executeDispatch(frame, SArguments.getEnvironment(frame), ExecutionLevel.Base, method, arguments);
    }
  }

  public MateAbstractSemanticsLevelNode getMateNode() {
    return semanticCheck;
  }

  public MateActivationDispatch getMateDispatch() {
    return reflectiveDispatch;
  }
  
  public AbstractMethodDispatchNode getDispatchListHead() {
    return methodDispatch;
  }

  public void adoptNewDispatchListHead(final AbstractMethodDispatchNode newHead) {
    CompilerAsserts.neverPartOfCompilation();
    methodDispatch = insert(newHead);
  }

  public void replaceDispatchListHead(
      final GenericMethodDispatchNode replacement) {
    CompilerAsserts.neverPartOfCompilation();
    methodDispatch.replace(replacement);
  }

  @Override
  public NodeCost getCost() {
    return NodeCost.NONE;
  }  
}
