package som.interpreter;

import java.util.List;

import som.compiler.Variable.Argument;
import som.compiler.Variable.Local;
import som.interpreter.nodes.ArgumentReadNode.LocalArgumentReadNode;
import som.interpreter.nodes.ArgumentReadNode.LocalSuperReadNode;
import som.interpreter.nodes.ArgumentReadNode.NonLocalArgumentReadNode;
import som.interpreter.nodes.ArgumentReadNode.NonLocalSuperReadNode;
import som.interpreter.nodes.ArgumentReadNode.ThisContextNode;
import som.interpreter.nodes.ContextualNode;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.FieldNode.FieldReadNode;
import som.interpreter.nodes.FieldNode.FieldWriteNode;
import som.interpreter.nodes.FieldNodeFactory.FieldReadNodeGen;
import som.interpreter.nodes.FieldNodeFactory.FieldWriteNodeGen;
import som.interpreter.nodes.GlobalNode;
import som.interpreter.nodes.GlobalNode.UninitializedGlobalReadNode;
import som.interpreter.nodes.LocalVariableNode.LocalVariableWriteNode;
import som.interpreter.nodes.LocalVariableNodeFactory.LocalVariableWriteNodeGen;
import som.interpreter.nodes.MessageSendNode;
import som.interpreter.nodes.MessageSendNode.AbstractMessageSendNode;
import som.interpreter.nodes.MessageSendNode.CascadeMessageSendNode;
import som.interpreter.nodes.NonLocalVariableNodeFactory.NonLocalArgumentVariableEmbeddedinBlockReadNodeUninitializedNodeGen;
import som.interpreter.nodes.ReturnNonLocalNode;
import som.interpreter.nodes.ReturnNonLocalNode.CatchNonLocalReturnNode;
import som.interpreter.nodes.SequenceNode;
import som.interpreter.nodes.UninitializedVariableNode.UninitializedVariableReadNode;
import som.interpreter.nodes.UninitializedVariableNode.UninitializedVariableWriteNode;
import som.interpreter.nodes.literals.BlockNode;
import som.interpreter.nodes.literals.BlockNode.BlockNodeWithContext;
import som.interpreter.nodes.nary.ExpressionWithReceiver;
import som.vm.ObjectMemory;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;



public final class SNodeFactory {

  public static CatchNonLocalReturnNode createCatchNonLocalReturn(
      final ExpressionNode methodBody, final FrameSlot frameOnStackMarker) {
    return new CatchNonLocalReturnNode(methodBody, frameOnStackMarker);
  }

  public static FieldReadNode createFieldRead(final ExpressionNode self,
      final int fieldIndex, final SourceSection source) {
    return FieldReadNodeGen.create(fieldIndex, source, self);
  }

  public static GlobalNode createGlobalRead(final String name,
      final ObjectMemory memory, final SourceSection source) {
    return createGlobalRead(memory.symbolFor(name), source);
  }
  public static GlobalNode createGlobalRead(final SSymbol name, final SourceSection source) {
    return new UninitializedGlobalReadNode(name, source);
  }

  public static FieldWriteNode createFieldWrite(final ExpressionNode self,
      final ExpressionNode exp, final int fieldIndex, final SourceSection source) {
    return FieldWriteNodeGen.create(fieldIndex, source, self, exp);
  }

  public static ContextualNode createLocalVarRead(final Local variable,
      final int contextLevel, final SourceSection source) {
    return new UninitializedVariableReadNode(variable, contextLevel, source);
  }
  
  public static ContextualNode createInlinedAsLocalArgumentVarRead(final Local variable,
      final int contextLevel, final SourceSection source) {
    return NonLocalArgumentVariableEmbeddedinBlockReadNodeUninitializedNodeGen.create(
        contextLevel, variable.getSlot(), source);
  }

  public static ExpressionNode createArgumentRead(final Argument variable,
      final int contextLevel, final SourceSection source) {
    if (contextLevel == 0) {
      return new LocalArgumentReadNode(variable.index, source);
    } else {
      return new NonLocalArgumentReadNode(variable.index, contextLevel, source);
    }
  }

  public static ExpressionNode createSuperRead(final int contextLevel,
        final SSymbol holderClass, final boolean classSide, final SourceSection source) {
    if (contextLevel == 0) {
      return new LocalSuperReadNode(holderClass, classSide, source);
    } else {
      return new NonLocalSuperReadNode(contextLevel, holderClass, classSide, source);
    }
  }

  public static ContextualNode createVariableWrite(final Local variable,
        final int contextLevel,
        final ExpressionNode exp, final SourceSection source) {
    return new UninitializedVariableWriteNode(variable, contextLevel, exp, source);
  }

  public static LocalVariableWriteNode createLocalVariableWrite(
      final FrameSlot varSlot, final ExpressionNode exp, final SourceSection source) {
    return LocalVariableWriteNodeGen.create(varSlot, source, exp);
  }

  public static SequenceNode createSequence(final List<ExpressionNode> exps,
      final SourceSection source) {
    return new SequenceNode(exps.toArray(new ExpressionNode[0]), source);
  }

  public static BlockNode createBlockNode(final DynamicObject blockMethod,
      final boolean withContext, final SourceSection source) {
    if (withContext) {
      return new BlockNodeWithContext(blockMethod, source);
    } else {
      return new BlockNode(blockMethod, source);
    }
  }

  public static AbstractMessageSendNode createMessageSend(final SSymbol msg,
      final ExpressionNode[] exprs, final SourceSection source) {
    return MessageSendNode.create(msg, exprs, source);
  }

  public static AbstractMessageSendNode createMessageSend(final SSymbol msg,
      final List<ExpressionNode> exprs, final SourceSection source) {
    return MessageSendNode.create(msg, exprs.toArray(new ExpressionNode[0]), source);
  }
  
  public static CascadeMessageSendNode createCascadeMessageSend(final ExpressionNode receiver,
      final List<ExpressionNode> messages, final SourceSection source) {
      return new CascadeMessageSendNode(receiver, messages.toArray(new ExpressionWithReceiver[0]), source);
  }
  
  public static ReturnNonLocalNode createNonLocalReturn(final ExpressionNode exp,
      final FrameSlot markerSlot, final int contextLevel,
      final SourceSection source) {
    return new ReturnNonLocalNode(exp, markerSlot, contextLevel, source);
  }
  
  public static ThisContextNode createThisContext(final SourceSection source){
    return new ThisContextNode(source);
  }
}
