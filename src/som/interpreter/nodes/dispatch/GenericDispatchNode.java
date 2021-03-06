package som.interpreter.nodes.dispatch;

import som.interpreter.SArguments;
import som.interpreter.Types;
import som.vm.constants.ExecutionLevel;
import som.vmobjects.SArray;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

public final class GenericDispatchNode extends AbstractDispatchNode {
  @Child private IndirectCallNode call;
  protected final SSymbol selector;
  protected final BranchProfile dnu = BranchProfile.create();

  public GenericDispatchNode(final SourceSection source, final SSymbol selector) {
    super(source);
    this.selector = selector;
    call = Truffle.getRuntime().createIndirectCallNode();
    this.adoptChildren();
  }

  @Override
  public Object executeDispatch(final VirtualFrame frame,
      final DynamicObject environment, final ExecutionLevel exLevel, final Object[] arguments) {
    Object rcvr = arguments[0];
    DynamicObject rcvrClass = Types.getClassOf(rcvr);
    DynamicObject method = SClass.lookupInvokable(rcvrClass, selector);

    CallTarget target;
    Object[] args;

    if (method != null) {
      target = SInvokable.getCallTarget(method, exLevel);
      args = SArguments.createSArguments(environment, exLevel, arguments);
    } else {
      // Won't use DNU caching here, because it is already a megamorphic node
      dnu.enter();
      SArray argumentsArray = SArguments.getArgumentsWithoutReceiver(arguments);
      args = new Object[] {environment, exLevel, arguments[SArguments.RCVR_ARGUMENTS_OFFSET], selector, argumentsArray};
      target = CachedDnuNode.getDnuCallTarget(rcvrClass, exLevel);
    }
    return call.call(target, args);
  }

  @Override
  public int lengthOfDispatchChain() {
    return 1000;
  }
}
