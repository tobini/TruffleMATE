package som.primitives;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SClass;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;


public class ContextPrims {

  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class GetMethodPrim extends UnaryExpressionNode {
    @TruffleBoundary
    @Specialization()
    public final SAbstractObject doSClass(final Object frame) {
      return this.getRootNode()
    }
  }
}
