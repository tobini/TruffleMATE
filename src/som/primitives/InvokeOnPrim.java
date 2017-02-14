package som.primitives;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.PreevaluatedExpression;
import som.interpreter.nodes.dispatch.InvokeOnCache;
import som.interpreter.nodes.nary.ExpressionWithTagsNode;
import som.primitives.arrays.ToArgumentsArrayNode;
import som.primitives.arrays.ToArgumentsArrayNodeFactory;
import som.vmobjects.SArray;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@NodeChildren({
  @NodeChild(value = "receiver", type = ExpressionNode.class),
  @NodeChild(value = "target",  type = ExpressionNode.class),
  @NodeChild(value = "somArr", type = ExpressionNode.class),
  @NodeChild(value = "argArr", type = ToArgumentsArrayNode.class,
             executeWith = {"somArr", "target"})})
@Primitive(klass = "Method", selector = "invokeOn:with:",
           noWrapper = true, extraChild = ToArgumentsArrayNodeFactory.class)
@Primitive(klass = "Primitive", selector = "invokeOn:with:",
           noWrapper = true, extraChild = ToArgumentsArrayNodeFactory.class,
           eagerSpecializable = false)
public abstract class InvokeOnPrim extends ExpressionWithTagsNode
  implements PreevaluatedExpression {
  @Child private InvokeOnCache callNode;

  public abstract ExpressionNode getReceiver();
  public abstract ExpressionNode getTarget();
  public abstract ExpressionNode getSomArr();
  public abstract ToArgumentsArrayNode getArgArr();

  public InvokeOnPrim(final boolean eagWrap, final SourceSection source) {
    super(source);
    callNode = InvokeOnCache.create();
  }

  public abstract Object executeEvaluated(VirtualFrame frame,
      DynamicObject receiver, Object target, SArray somArr);

  @Override
  public final Object doPreEvaluated(final VirtualFrame frame,
      final Object[] args) {
    return executeEvaluated(frame, (DynamicObject) args[0], args[1], (SArray) args[2]);
  }

  @Specialization
  public final Object doInvoke(final VirtualFrame frame,
      final DynamicObject receiver, final Object target, final SArray somArr,
      final Object[] argArr) {
    return callNode.executeDispatch(frame, receiver, argArr);
  }

  public Object[] evaluateArguments(final VirtualFrame frame) {
    return this.getArgArr().executedEvaluated(this.getSomArr().executeGeneric(frame), this.getTarget().executeGeneric(frame));
  }
}
