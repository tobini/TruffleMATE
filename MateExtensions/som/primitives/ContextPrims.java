package som.primitives;

import som.interpreter.Invokable;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SClass;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.impl.DefaultCallTarget;
import com.oracle.truffle.api.object.DynamicObject;


public class ContextPrims {
  @GenerateNodeFactory
  @ImportStatic(SClass.class)
  public abstract static class GetMethodPrim extends UnaryExpressionNode {
    @Specialization
    public final DynamicObject doMaterializedFrame(final Object frame) {
      DefaultCallTarget target = ((DefaultCallTarget)((FrameInstance)frame).getCallTarget());
      return ((Invokable)target.getRootNode()).getBelongsToMethod();
    }
  }
}
