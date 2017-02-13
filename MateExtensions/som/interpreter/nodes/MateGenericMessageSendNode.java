package som.interpreter.nodes;

import som.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import som.interpreter.nodes.dispatch.AbstractDispatchNode;
import som.matenodes.IntercessionHandling;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


public class MateGenericMessageSendNode extends GenericMessageSendNode {
  @Child private IntercessionHandling ih;

  protected MateGenericMessageSendNode(final SSymbol selector,
      final ExpressionNode[] arguments,
      final AbstractDispatchNode dispatchNode, final SourceSection source) {
    super(selector, arguments, dispatchNode, source);
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
}
