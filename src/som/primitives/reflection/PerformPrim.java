package som.primitives.reflection;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
public abstract class PerformPrim extends BinaryExpressionNode {
  @Child protected AbstractSymbolDispatch dispatch;

  public PerformPrim() { 
    super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Perform")); 
    dispatch = AbstractSymbolDispatchNodeGen.create(); 
  }

  @Specialization
  public final Object doObject(final VirtualFrame frame, final Object receiver, final SSymbol selector) {
    return dispatch.
        executeDispatch(frame, receiver, selector, null);
  }
}
