package som.primitives.reflection;

import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.vmobjects.SArray;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;


@GenerateNodeFactory
public abstract class PerformWithArgumentsPrim extends TernaryExpressionNode {

  @Child protected AbstractSymbolDispatch dispatch;

  public PerformWithArgumentsPrim() {
    dispatch = AbstractSymbolDispatchNodeGen.create();
  }

  @Specialization
  public final Object doObject(final VirtualFrame frame,
      final Object receiver, final SSymbol selector, final SArray  argsArr) {
    return dispatch.executeDispatch(frame, receiver, selector, argsArr);
  }
  
  /*Todo: Remove the conversion to SArray and then conversion to normal array in the dispatch if a performance problem emerges*/
  @Specialization
  public final Object doObject(final VirtualFrame frame,
      final Object receiver, final SSymbol selector, final long arg) {
    return dispatch.executeDispatch(frame, receiver, selector, SArray.create(new Object[]{arg}));
  }
  
  @Specialization
  public final Object doObject(final VirtualFrame frame,
      final Object receiver, final SSymbol selector, final double arg) {
    return dispatch.executeDispatch(frame, receiver, selector, SArray.create(new Object[]{arg}));
  }
  
  @Specialization
  public final Object doObject(final VirtualFrame frame,
      final Object receiver, final SSymbol selector, final DynamicObject arg) {
    return dispatch.executeDispatch(frame, receiver, selector, SArray.create(new Object[]{arg}));
  }
  
  @Override
  public NodeCost getCost() {
    return dispatch.getCost();
  }
}
