package som.interpreter.nodes.specialized;

import som.interpreter.SArguments;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.literals.BlockNode;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.specialized.AndMessageNodeFactory.AndBoolMessageNodeFactory;
import som.primitives.Primitive;
import som.primitives.Primitives.Specializer;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SBlock;
import som.vmobjects.SInvokable;
import tools.dym.Tags.ControlFlowCondition;
import tools.dym.Tags.OpComparison;
import som.interpreter.nodes.specialized.AndMessageNode.AndOrSplzr;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@Primitive(selector = "and:", noWrapper = true, specializer = AndOrSplzr.class)
@Primitive(selector = "&&",   noWrapper = true, specializer = AndOrSplzr.class)
public abstract class AndMessageNode extends BinaryExpressionNode {
  private final DynamicObject blockMethod;
  @Child private DirectCallNode blockValueSend;

  public static class AndOrSplzr extends Specializer<BinaryExpressionNode> {
    protected final NodeFactory<BinaryExpressionNode> boolFact;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AndOrSplzr(final Primitive prim, final NodeFactory<BinaryExpressionNode> fact) {
      this(prim, fact, (NodeFactory) AndBoolMessageNodeFactory.getInstance());
    }

    protected AndOrSplzr(final Primitive prim, final NodeFactory<BinaryExpressionNode> msgFact,
        final NodeFactory<BinaryExpressionNode> boolFact) {
      super(prim, msgFact);
      this.boolFact = boolFact;
    }

    @Override
    public final boolean matches(final Object[] args, final ExpressionNode[] argNodes) {
      return args[0] instanceof Boolean &&
          (args[1] instanceof Boolean ||
              unwrapIfNecessary(argNodes[1]) instanceof BlockNode);
    }

    @Override
    public final BinaryExpressionNode create(final Object[] arguments,
        final ExpressionNode[] argNodes, final SourceSection section,
        final boolean eagerWrapper, final Frame frame) {
      if (unwrapIfNecessary(argNodes[1]) instanceof BlockNode) {
        return fact.createNode(arguments[1], section, SArguments.getExecutionLevel(frame), argNodes[0], argNodes[1]);
      } else {
        assert arguments[1] instanceof Boolean;
        return boolFact.createNode(section, argNodes[0], argNodes[1]);
      }
    }
  }

  public AndMessageNode(final SBlock arg, final SourceSection source, ExecutionLevel level) {
    super(false, source);
    blockMethod = arg.getMethod();
    blockValueSend = Truffle.getRuntime().createDirectCallNode(
        SInvokable.getCallTarget(blockMethod, level));
  }

  protected final boolean isSameBlock(final SBlock argument) {
    return argument.getMethod() == blockMethod;
  }

  @Specialization(guards = "isSameBlock(argument)")
  public final boolean doAnd(final VirtualFrame frame, final boolean receiver,
      final SBlock argument) {
    if (receiver == false) {
      return false;
    } else {
      return (boolean) blockValueSend.call(new Object[] {SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), argument});
    }
  }

  @GenerateNodeFactory
  public abstract static class AndBoolMessageNode extends BinaryExpressionNode {

    public AndBoolMessageNode(final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final boolean doAnd(final VirtualFrame frame, final boolean receiver, final boolean argument) {
      return receiver && argument;
    }
  }

  @Override
  protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
    if (tag == ControlFlowCondition.class) {
      return true;
    } else if (tag == OpComparison.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
