package som.primitives;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;


public class ContextPrims {

  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class GetMethodPrim extends UnaryExpressionNode {
    @TruffleBoundary
    @Specialization
    public final SAbstractObject doSClass(final Object frame) {
      //return (ExpressionNode)this.findEnclosingMethod();
      return null;
    }
  }
}
