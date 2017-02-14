package som.primitives.reflection;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.primitives.Primitive;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@Primitive(klass = "Object", selector = "perform:")
public abstract class PerformPrim extends BinaryExpressionNode {
  @Child protected AbstractSymbolDispatch dispatch;

  public PerformPrim(final boolean eagWrap, final SourceSection source) {
    super(false, source);
    dispatch = AbstractSymbolDispatchNodeGen.create(source);
  }

  @Specialization
  public final Object doObject(final VirtualFrame frame, final Object receiver, final SSymbol selector) {
    return dispatch.
        executeDispatch(frame, receiver, selector, null);
  }
}
