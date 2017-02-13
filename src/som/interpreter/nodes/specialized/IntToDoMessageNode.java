package som.interpreter.nodes.specialized;

import som.VmSettings;
import som.interpreter.Invokable;
import som.interpreter.SArguments;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.interpreter.nodes.specialized.IntToDoMessageNode.ToDoSplzr;
import som.primitives.Primitive;
import som.primitives.Primitives.Specializer;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SBlock;
import som.vmobjects.SInvokable;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
//Should have noWrapper = true?
@GenerateNodeFactory
@Primitive(selector = "to:do:", disabled = true,
           specializer = ToDoSplzr.class, requiresArguments = true,
           requiresExecutionLevel = true)
public abstract class IntToDoMessageNode extends TernaryExpressionNode {
  public static class ToDoSplzr extends Specializer<IntToDoMessageNode> {
    public ToDoSplzr(final Primitive prim, final NodeFactory<IntToDoMessageNode> fact) { super(prim, fact); }

    @Override
    public boolean matches(final Object[] args,
        final ExpressionNode[] argNodes) {
      return !VmSettings.DYNAMIC_METRICS && args[0] instanceof Long &&
          (args[1] instanceof Long || args[1] instanceof Double) &&
          args[2] instanceof SBlock;
    }
  }

  private final DynamicObject blockMethod;
  @Child private DirectCallNode valueSend;

  public IntToDoMessageNode(final boolean eagWrap, final SourceSection source,
      final Object[] args, ExecutionLevel level) {
    super(false, source);
    blockMethod = ((SBlock) args[2]).getMethod();
    valueSend = Truffle.getRuntime().createDirectCallNode(
                    SInvokable.getCallTarget(blockMethod, level));
  }

  protected final boolean isSameBlockLong(final SBlock block) {
    return block.getMethod() == blockMethod;
  }

  @Specialization(guards = "isSameBlockLong(block)")
  public final long doIntToDo(final VirtualFrame frame, final long receiver, final long limit, final SBlock block) {
    try {
      doLooping(frame, receiver, limit, block);
    } finally {
      if (CompilerDirectives.inInterpreter() && (limit - receiver) > 0) {
        reportLoopCount(limit - receiver);
      }
    }
    return receiver;
  }

  protected final boolean isSameBlockDouble(final SBlock block) {
    return block.getMethod() == blockMethod;
  }

  @Specialization(guards = "isSameBlockDouble(block)")
  public final long doIntToDo(final VirtualFrame frame, final long receiver, final double dLimit, final SBlock block) {
    long limit = (long) dLimit;
    try {
      doLooping(frame, receiver, limit, block);
    } finally {
      if (CompilerDirectives.inInterpreter()) {
        reportLoopCount((int) limit - receiver);
      }
    }
    return receiver;
  }

  protected void doLooping(final VirtualFrame frame, final long receiver,
      long limit, final SBlock block) {
    if (receiver <= limit) {
      valueSend.call(new Object[] {SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), block, receiver});
    }
    for (long i = receiver + 1; i <= limit; i++) {
      valueSend.call(new Object[] {SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), block, i});
    }
  }

  private void reportLoopCount(final long count) {
    CompilerAsserts.neverPartOfCompilation("reportLoopCount");
    Node current = getParent();
    while (current != null && !(current instanceof RootNode)) {
      current = current.getParent();
    }
    if (current != null) {
      ((Invokable) current).propagateLoopCountThroughoutLexicalScope(count);
    }
  }

  @Override
  protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
    if (tag == LoopNode.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
