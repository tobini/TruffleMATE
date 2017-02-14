package som.interpreter.nodes.specialized;

import som.interpreter.Invokable;
import som.interpreter.SArguments;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.primitives.Primitive;
import som.vm.Universe;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SBlock;
import som.vmobjects.SInvokable;
import som.interpreter.nodes.specialized.IntToDoMessageNode.ToDoSplzr;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@Primitive(selector = "downTo:do:", noWrapper = true, disabled = true,
           requiresExecutionLevel = true, requiresArguments = true,
           specializer = ToDoSplzr.class)
@GenerateNodeFactory
public abstract class IntDownToDoMessageNode extends TernaryExpressionNode {

  private final DynamicObject blockMethod;
  @Child private DirectCallNode valueSend;

  public IntDownToDoMessageNode(final boolean eagPrim, final SourceSection source,
      final Object[] args, final ExecutionLevel level) {
    super(false, source);
    blockMethod = ((SBlock) args[2]).getMethod();
    valueSend = Truffle.getRuntime().createDirectCallNode(
                    SInvokable.getCallTarget(blockMethod, level));
  }

  protected final boolean isSameBlockLong(final SBlock block) {
    return block.getMethod() == blockMethod;
  }

  @Specialization(guards = "isSameBlockLong(block)")
  public final long doIntDownToDo(final VirtualFrame frame, final long receiver, final long limit, final SBlock block) {
    try {
      if (receiver >= limit) {
        valueSend.call(new Object[] {SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), block, receiver});
      }
      for (long i = receiver - 1; i >= limit; i--) {
        valueSend.call(new Object[] {SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), block, i});
      }
    } finally {
      if (CompilerDirectives.inInterpreter() && (receiver - limit) > 0) {
        reportLoopCount(receiver - limit);
      }
    }
    return receiver;
  }

  protected final boolean isSameBlockDouble(final SBlock block) {
    return block.getMethod() == blockMethod;
  }

  @Specialization(guards = "isSameBlockDouble(block)")
  public final long doIntDownToDo(final VirtualFrame frame, final long receiver, final double limit, final SBlock block) {
    try {
      if (receiver >= limit) {
        valueSend.call(new Object[] {block, receiver});
      }
      for (long i = receiver - 1; i >= limit; i--) {
        valueSend.call(new Object[] {block, i});
      }
    } finally {
      if (CompilerDirectives.inInterpreter() && (receiver - (int) limit) > 0) {
        reportLoopCount(receiver - (int) limit);
      }
    }
    return receiver;
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
  public void wrapIntoMateNode() {
    super.wrapIntoMateNode();
    Universe.getCurrent().mateifyMethod(blockMethod);
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
