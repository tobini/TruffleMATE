package som.matenodes;

import som.interpreter.nodes.ISuperReadNode;
import som.matenodes.MateAbstractReflectiveDispatch.MateAbstractStandardDispatch;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateCachedDispatchMessageLookupNodeGen;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateCachedDispatchSuperMessageLookupNodeGen;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateDispatchFieldReadNodeGen;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateDispatchFieldWriteNodeGen;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateDispatchPrimFieldReadNodeGen;
import som.matenodes.MateAbstractReflectiveDispatchFactory.MateDispatchPrimFieldWriteNodeGen;
import som.matenodes.MateAbstractSemanticNodes.MateAbstractSemanticsLevelNode;
import som.matenodes.MateAbstractSemanticNodesFactory.MateSemanticCheckNodeGen;
import som.vm.Universe;
import som.vm.constants.ReflectiveOp;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

public abstract class IntercessionHandling extends Node {
  public abstract Object doMateSemantics(final VirtualFrame frame,
      final Object[] arguments);
  
  public static IntercessionHandling createForOperation(ReflectiveOp operation){
    if (operation == ReflectiveOp.None){
      return new VoidIntercessionHandling();
    } else {
      return new MateIntercessionHandling(operation);
    }
  }
  
  public static IntercessionHandling createForMessageLookup(SSymbol selector){
    return new MateIntercessionHandling(selector);
  }
  
  public static IntercessionHandling createForSuperMessageLookup(SSymbol selector, ISuperReadNode node){
    return new MateIntercessionHandling(selector, node);
  }
  
  public static class VoidIntercessionHandling extends IntercessionHandling {
    @Override
    public Object doMateSemantics(final VirtualFrame frame,
        final Object[] arguments) {
      return null;
    }
  }
  
  public static class MateIntercessionHandling extends IntercessionHandling {
    @Child MateAbstractSemanticsLevelNode   semanticCheck;
    @Child MateAbstractStandardDispatch     reflectiveDispatch;
    private final BranchProfile semanticsRedefined = BranchProfile.create();
    
    protected MateIntercessionHandling(ReflectiveOp operation){
      semanticCheck = MateSemanticCheckNodeGen.create(SourceSection.createUnavailable("", ""), operation);
      switch (operation){
        case LayoutReadField: case ExecutorReadField:
          reflectiveDispatch = MateDispatchFieldReadNodeGen.create();
          break;
        case LayoutPrimReadField:
          reflectiveDispatch = MateDispatchPrimFieldReadNodeGen.create();
          break;
        case LayoutWriteField: case ExecutorWriteField:
          reflectiveDispatch = MateDispatchFieldWriteNodeGen.create();
          break;
        case LayoutPrimWriteField:
          reflectiveDispatch = MateDispatchPrimFieldWriteNodeGen.create();
          break;
        case ExecutorLocalArg: case ExecutorNonLocalArg: case ExecutorLocalSuperArg: case ExecutorNonLocalSuperArg:
          reflectiveDispatch = MateDispatchFieldReadNodeGen.create();
          break;
        case ExecutorReadLocal: case ExecutorWriteLocal:
          reflectiveDispatch = MateDispatchFieldReadNodeGen.create();
          break;
        case ExecutorReturn:
          reflectiveDispatch = MateDispatchFieldReadNodeGen.create();
          break;
        default:
          Universe.errorExit("Unexepected operation");
      }
      this.adoptChildren();
    }
    
    protected MateIntercessionHandling(SSymbol selector){
      semanticCheck = MateSemanticCheckNodeGen.create(SourceSection.createUnavailable("", ""), ReflectiveOp.MessageLookup);
      reflectiveDispatch = MateCachedDispatchMessageLookupNodeGen.create(selector);
      this.adoptChildren();
    }
    
    protected MateIntercessionHandling(SSymbol selector, ISuperReadNode node){
      semanticCheck = MateSemanticCheckNodeGen.create(SourceSection.createUnavailable("", ""), ReflectiveOp.MessageLookup);
      reflectiveDispatch = MateCachedDispatchSuperMessageLookupNodeGen.create(selector, node);
      this.adoptChildren();
    }
    
    @Override
    public Object doMateSemantics(final VirtualFrame frame,
        final Object[] arguments) {
      DynamicObject method = this.getMateNode().execute(frame, arguments);
      if (method != null){
        semanticsRedefined.enter();
        return this.getMateDispatch().executeDispatch(frame, method, arguments[0], arguments);
      } 
      return null;      
    }
    
    private MateAbstractSemanticsLevelNode getMateNode() {
      return this.semanticCheck;
    }

    private MateAbstractStandardDispatch getMateDispatch() {
      return this.reflectiveDispatch;
    }
  }

}