package som.interpreter.nodes;

import som.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import som.interpreter.nodes.MessageSendNode.UninitializedMessageSendNode;
import som.interpreter.nodes.dispatch.UninitializedDispatchNode;
import som.matenodes.IntercessionHandling;
import com.oracle.truffle.api.frame.VirtualFrame;

public class MateUninitializedMessageSendNode extends
    UninitializedMessageSendNode {
  @Child private IntercessionHandling ih;

  public MateUninitializedMessageSendNode(UninitializedMessageSendNode somNode) {
    super(somNode.getSelector(), somNode.argumentNodes, somNode.getSourceSection());
    if (this.isSuperSend()) {
      ih = IntercessionHandling.createForSuperMessageLookup(this.getSelector(), (ISuperReadNode) this.argumentNodes[0]);
    } else {
      ih = IntercessionHandling.createForMessageLookup(this.getSelector());
    }
    this.adoptChildren();
  }

  @Override
  public final Object executeGeneric(final VirtualFrame frame) {
    Object[] arguments = evaluateArguments(frame);
    Object value = ih.doMateSemantics(frame, arguments);
    if (value == null) {
     value = doPreEvaluated(frame, arguments);
    }
    return value;
  }

  @Override
  public ExpressionNode asMateNode() {
    return null;
  }

  @Override
  protected GenericMessageSendNode makeGenericSend() {
    GenericMessageSendNode send = new MateGenericMessageSendNode(selector,
        argumentNodes,
        new UninitializedDispatchNode(this.sourceSection, selector),
        getSourceSection());
    return replace(send);
  }
}
