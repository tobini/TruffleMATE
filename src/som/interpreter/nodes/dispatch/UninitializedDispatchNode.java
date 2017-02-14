package som.interpreter.nodes.dispatch;

import static som.interpreter.TruffleCompiler.transferToInterpreterAndInvalidate;
import som.interpreter.SArguments;
import som.interpreter.Types;
import som.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import som.vm.Universe;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


public final class UninitializedDispatchNode extends AbstractDispatchNode {
  protected final SSymbol selector;

  public UninitializedDispatchNode(final SourceSection source, final SSymbol selector) {
    super(source);
    this.selector = selector;
  }

  private AbstractDispatchNode specialize(final VirtualFrame frame, final Object[] arguments) {
    // Determine position in dispatch node chain, i.e., size of inline cache
    Node i = this;
    int chainDepth = 0;
    while (i.getParent() instanceof AbstractDispatchNode) {
      i = i.getParent();
      chainDepth++;
    }
    AbstractDispatchNode first = (AbstractDispatchNode) i;
    Object rcvr = arguments[0];
    assert rcvr != null;

    if (chainDepth < INLINE_CACHE_SIZE) {
      DynamicObject rcvrClass = Types.getClassOf(rcvr);
      DynamicObject method = SClass.lookupInvokable(rcvrClass, selector);
      CallTarget callTarget;
      if (method != null) {
        callTarget = SInvokable.getCallTarget(method, SArguments.getExecutionLevel(frame));
      } else {
        callTarget = null;
      }

      UninitializedDispatchNode newChainEnd = new UninitializedDispatchNode(this.sourceSection, selector);
      DispatchGuard guard = DispatchGuard.create(rcvr);
      AbstractCachedDispatchNode node;
      if (method != null) {
        node = new CachedDispatchNode(guard, callTarget, newChainEnd);
      } else {
        node = new CachedDnuNode(rcvrClass, guard, selector, newChainEnd, SArguments.getExecutionLevel(frame));
      }
      Universe.insertInstrumentationWrapper(this);
      replace(node);
      Universe.insertInstrumentationWrapper(node);
      return node;
    }

    // the chain is longer than the maximum defined by INLINE_CACHE_SIZE and
    // thus, this callsite is considered to be megaprophic, and we generalize
    // it.
    GenericDispatchNode genericReplacement = new GenericDispatchNode(this.sourceSection, selector);
    GenericMessageSendNode sendNode = (GenericMessageSendNode) first.getParent();
    sendNode.replaceDispatchListHead(genericReplacement);
    return genericReplacement;
  }

  @Override
  public Object executeDispatch(final VirtualFrame frame,
      final DynamicObject environment, final ExecutionLevel exLevel, final Object[] arguments) {
    transferToInterpreterAndInvalidate("Initialize a dispatch node.");
    return specialize(frame, arguments).
        executeDispatch(frame, environment, exLevel, arguments);
  }

  @Override
  public int lengthOfDispatchChain() {
    return 0;
  }
}
