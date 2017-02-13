package som.primitives;

import som.interpreter.FrameOnStackMarker;
import som.interpreter.Invokable;
import som.interpreter.MateVisitors;
import som.interpreter.SArguments;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


public class ContextPrims {
  @GenerateNodeFactory
  @Primitive(klass = "Context", selector = "method", receiverType = {FrameInstance.class})
  public abstract static class GetMethodPrim extends UnaryExpressionNode {
    public GetMethodPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final DynamicObject doMaterializedFrame(final FrameInstance frame) {
      RootCallTarget target = ((RootCallTarget) frame.getCallTarget());
      return ((Invokable) target.getRootNode()).getBelongsToMethod();
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Context", selector = "sender", receiverType = {FrameInstance.class})
  public abstract static class SenderPrim extends UnaryExpressionNode {
    public SenderPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final FrameInstance doMaterializedFrame(final FrameInstance frame) {
      TruffleRuntime runtime = ((Universe) ((ExpressionNode) this).getRootNode().getExecutionContext()).getTruffleRuntime();
      FrameInstance sender;
      if (runtime.getCurrentFrame() == frame) {
        sender = runtime.getCallerFrame();
      } else {
        sender = runtime.iterateFrames(new MateVisitors.FindSenderFrame(frame.getFrame(FrameAccess.MATERIALIZE)));
      }
      Frame senderFrame = sender.getFrame(FrameAccess.MATERIALIZE);
      if (senderFrame.getFrameDescriptor().findFrameSlot(Universe.frameOnStackSlotName()) == null) {
        senderFrame.setObject(
            senderFrame.getFrameDescriptor().addFrameSlot(Universe.frameOnStackSlotName(), FrameSlotKind.Object),
            new FrameOnStackMarker());
      }
      return sender;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Context", selector = "receiver", receiverType = {FrameInstance.class})
  public abstract static class GetReceiverFromContextPrim extends UnaryExpressionNode {
    public GetReceiverFromContextPrim(final boolean eagWrap, SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final DynamicObject doMaterializedFrame(final FrameInstance frame) {
      Frame virtualFrame = frame.getFrame(FrameAccess.READ_ONLY);
      return (DynamicObject) SArguments.rcvr(virtualFrame);
    }
  }
}
