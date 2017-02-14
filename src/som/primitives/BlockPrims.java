package som.primitives;

import som.interpreter.SArguments;
import som.interpreter.nodes.dispatch.AbstractDispatchNode;
import som.interpreter.nodes.dispatch.UninitializedValuePrimDispatchNode;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.QuaternaryExpressionNode;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SBlock;
import tools.dym.Tags.OpClosureApplication;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.source.SourceSection;


public abstract class BlockPrims {

  public interface ValuePrimitiveNode {
    void adoptNewDispatchListHead(AbstractDispatchNode node);
  }

  @GenerateNodeFactory
  @Primitive(klass = "Block", selector = "restart", eagerSpecializable = false)
  public abstract static class RestartPrim extends UnaryExpressionNode {
    public RestartPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public SAbstractObject doSBlock(final SBlock receiver) {
      CompilerDirectives.transferToInterpreter();
      // TruffleSOM intrinsifies #whileTrue: and #whileFalse:
      throw new RuntimeException("This primitive is not supported anymore! "
          + "Something went wrong with the intrinsification of "
          + "#whileTrue:/#whileFalse:?");
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Block1", selector = "value",
             receiverType = {SBlock.class, Boolean.class})
  public abstract static class ValueNonePrim extends UnaryExpressionNode
      implements ValuePrimitiveNode {
    @Child private AbstractDispatchNode dispatchNode;

    public ValueNonePrim(final boolean eagWrap) {
      super(eagWrap, Universe.emptySource.createUnavailableSection());
    }

    public ValueNonePrim(final boolean eagerlyWrapped, SourceSection source) {
      super(eagerlyWrapped, source);
      dispatchNode = new UninitializedValuePrimDispatchNode(this.sourceSection);
    }

    @Specialization
    public final Object doSBlock(final VirtualFrame frame, final SBlock receiver) {
      return dispatchNode.executeDispatch(frame, SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), new Object[] {receiver});
    }

    @Specialization
    public final boolean doBoolean(final boolean receiver) {
      return receiver;
    }

    @Override
    public final void adoptNewDispatchListHead(final AbstractDispatchNode node) {
      dispatchNode = insert(node);
    }

    @Override
    public NodeCost getCost() {
      int dispatchChain = dispatchNode.lengthOfDispatchChain();
      if (dispatchChain == 0) {
        return NodeCost.UNINITIALIZED;
      } else if (dispatchChain == 1) {
        return NodeCost.MONOMORPHIC;
      } else if (dispatchChain <= AbstractDispatchNode.INLINE_CACHE_SIZE) {
        return NodeCost.POLYMORPHIC;
      } else {
        return NodeCost.MEGAMORPHIC;
      }
    }

    @Override
    protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
      if (tag == OpClosureApplication.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Block2", selector = "value:", receiverType = {SBlock.class})
  public abstract static class ValueOnePrim extends BinaryExpressionNode
      implements ValuePrimitiveNode  {
    @Child private AbstractDispatchNode dispatchNode;

    public ValueOnePrim(final boolean eagWrap) {
      this(eagWrap, null);
    }

    public ValueOnePrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
      dispatchNode = new UninitializedValuePrimDispatchNode(this.sourceSection);
    }

    @Specialization
    public final Object doSBlock(final VirtualFrame frame, final SBlock receiver,
        final Object arg) {
      return dispatchNode.executeDispatch(frame, SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), new Object[] {receiver, arg});
    }

    @Override
    public final void adoptNewDispatchListHead(final AbstractDispatchNode node) {
      dispatchNode = insert(node);
    }

    @Override
    public NodeCost getCost() {
      int dispatchChain = dispatchNode.lengthOfDispatchChain();
      if (dispatchChain == 0) {
        return NodeCost.UNINITIALIZED;
      } else if (dispatchChain == 1) {
        return NodeCost.MONOMORPHIC;
      } else if (dispatchChain <= AbstractDispatchNode.INLINE_CACHE_SIZE) {
        return NodeCost.POLYMORPHIC;
      } else {
        return NodeCost.MEGAMORPHIC;
      }
    }

    @Override
    protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
      if (tag == OpClosureApplication.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Block3", selector = "value:with:",
      receiverType = {SBlock.class})
  public abstract static class ValueTwoPrim extends TernaryExpressionNode
      implements ValuePrimitiveNode {
    @Child private AbstractDispatchNode dispatchNode;

    public ValueTwoPrim(final boolean eagerlyWrapped) {
      this(eagerlyWrapped, null);
    }

    public ValueTwoPrim(final boolean eagerlyWrapped, SourceSection source) {
      super(eagerlyWrapped, source);
      dispatchNode = new UninitializedValuePrimDispatchNode(this.sourceSection);
    }

    @Specialization
    public final Object doSBlock(final VirtualFrame frame,
        final SBlock receiver, final Object arg1, final Object arg2) {
      return dispatchNode.executeDispatch(frame, SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), new Object[] {receiver, arg1, arg2});
    }

    @Override
    public final void adoptNewDispatchListHead(final AbstractDispatchNode node) {
      dispatchNode = insert(node);
    }

    @Override
    public NodeCost getCost() {
      int dispatchChain = dispatchNode.lengthOfDispatchChain();
      if (dispatchChain == 0) {
        return NodeCost.UNINITIALIZED;
      } else if (dispatchChain == 1) {
        return NodeCost.MONOMORPHIC;
      } else if (dispatchChain <= AbstractDispatchNode.INLINE_CACHE_SIZE) {
        return NodeCost.POLYMORPHIC;
      } else {
        return NodeCost.MEGAMORPHIC;
      }
    }

    @Override
    protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
      if (tag == OpClosureApplication.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Block4", selector = "value:with:with:",
  receiverType = {SBlock.class})
  public abstract static class ValueThreePrim extends QuaternaryExpressionNode
      implements ValuePrimitiveNode {
    @Child private AbstractDispatchNode dispatchNode;

    public ValueThreePrim(final boolean eagerlyWrapped) {
      this(eagerlyWrapped, null);
    }

    public ValueThreePrim(final boolean eagerlyWrapped, SourceSection source) {
      super(eagerlyWrapped, source);
      dispatchNode = new UninitializedValuePrimDispatchNode(this.sourceSection);
    }

    @Specialization
    public final Object doSBlock(final VirtualFrame frame,
        final SBlock receiver, final Object arg1, final Object arg2, final Object arg3) {
      return dispatchNode.executeDispatch(frame, SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), new Object[] {receiver, arg1, arg2, arg3});
    }

    @Override
    public final void adoptNewDispatchListHead(final AbstractDispatchNode node) {
      dispatchNode = insert(node);
    }

    @Override
    public NodeCost getCost() {
      int dispatchChain = dispatchNode.lengthOfDispatchChain();
      if (dispatchChain == 0) {
        return NodeCost.UNINITIALIZED;
      } else if (dispatchChain == 1) {
        return NodeCost.MONOMORPHIC;
      } else if (dispatchChain <= AbstractDispatchNode.INLINE_CACHE_SIZE) {
        return NodeCost.POLYMORPHIC;
      } else {
        return NodeCost.MEGAMORPHIC;
      }
    }

    @Override
    protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
      if (tag == OpClosureApplication.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }

  /*@GenerateNodeFactory
  @Primitive(klass = "Block5", selector = "value:with:with:with:",
             receiverType = {SBlock.class})
  public abstract static class ValueMorePrim extends QuaternaryExpressionNode {
    public ValueMorePrim(final boolean eagerlyWrapped) {this(eagerlyWrapped, null);}
    public ValueMorePrim(final boolean eagerlyWrapped, SourceSection source) { super(eagerlyWrapped, source); }
    @Specialization
    public final Object doSBlock(final VirtualFrame frame,
        final SBlock receiver, final Object firstArg, final Object secondArg,
        final Object thirdArg) {
      CompilerDirectives.transferToInterpreter();
      throw new RuntimeException("This should never be called, because SOM Blocks have max. 2 arguments.");
    }
  }*/
}
