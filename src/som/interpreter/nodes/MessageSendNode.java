package som.interpreter.nodes;

import som.instrumentation.MessageSendNodeWrapper;
import som.interpreter.SArguments;
import som.interpreter.TypesGen;
import som.interpreter.TruffleCompiler;
import som.interpreter.nodes.dispatch.AbstractDispatchNode;
import som.interpreter.nodes.dispatch.DispatchChain.Cost;
import som.interpreter.nodes.dispatch.GenericDispatchNode;
import som.interpreter.nodes.dispatch.SuperDispatchNode;
import som.interpreter.nodes.dispatch.UninitializedDispatchNode;
import som.interpreter.nodes.literals.BlockNode;
import som.interpreter.nodes.nary.EagerPrimitive;
import som.interpreter.nodes.nary.ExpressionWithReceiver;
import som.interpreter.nodes.nary.ExpressionWithTagsNode;
import som.interpreter.nodes.specialized.AndMessageNodeFactory;
import som.interpreter.nodes.specialized.AndMessageNodeFactory.AndBoolMessageNodeFactory;
import som.interpreter.nodes.specialized.IfMessageNodeGen;
import som.interpreter.nodes.specialized.IfTrueIfFalseMessageNodeGen;
import som.interpreter.nodes.specialized.IntDownToDoMessageNodeGen;
import som.interpreter.nodes.specialized.IntToByDoMessageNodeGen;
import som.interpreter.nodes.specialized.IntToDoMessageNodeGen;
import som.interpreter.nodes.specialized.NotMessageNodeFactory;
import som.interpreter.nodes.specialized.OrMessageNodeGen;
import som.interpreter.nodes.specialized.OrMessageNodeGen.OrBoolMessageNodeGen;
import som.interpreter.nodes.specialized.whileloops.WhileWithDynamicBlocksNode;
import som.interpreter.nodes.specialized.whileloops.WhileWithStaticBlocksNode.WhileFalseStaticBlocksNode;
import som.interpreter.nodes.specialized.whileloops.WhileWithStaticBlocksNode.WhileTrueStaticBlocksNode;
import som.primitives.BlockPrimsFactory.ValueNonePrimFactory;
import som.primitives.BlockPrimsFactory.ValueOnePrimFactory;
import som.primitives.EqualsEqualsPrimFactory;
import som.primitives.EqualsPrimFactory;
import som.primitives.IntegerPrimsFactory.AbsPrimFactory;
import som.primitives.IntegerPrimsFactory.LeftShiftPrimFactory;
import som.primitives.IntegerPrimsFactory.ToPrimFactory;
import som.primitives.IntegerPrimsFactory.UnsignedRightShiftPrimFactory;
import som.primitives.LengthPrimFactory;
import som.primitives.MethodPrimsFactory.InvokeOnPrimFactory;
import som.primitives.ObjectPrimsFactory.InstVarAtPrimFactory;
import som.primitives.ObjectPrimsFactory.InstVarAtPutPrimFactory;
import som.primitives.UnequalsPrimFactory;
import som.primitives.arithmetic.AdditionPrimFactory;
import som.primitives.arithmetic.BitXorPrimFactory;
import som.primitives.arithmetic.DividePrimFactory;
import som.primitives.arithmetic.DoubleDivPrimFactory;
import som.primitives.arithmetic.GreaterThanPrimFactory;
import som.primitives.arithmetic.LessThanOrEqualPrimFactory;
import som.primitives.arithmetic.LessThanPrimFactory;
import som.primitives.arithmetic.LogicAndPrimFactory;
import som.primitives.arithmetic.ModuloPrimFactory;
import som.primitives.arithmetic.MultiplicationPrimFactory;
import som.primitives.arithmetic.RemainderPrimFactory;
import som.primitives.arithmetic.SubtractionPrimFactory;
import som.primitives.arrays.AtPrimFactory;
import som.primitives.arrays.AtPutPrimFactory;
import som.primitives.arrays.DoIndexesPrimFactory;
import som.primitives.arrays.DoPrimFactory;
import som.primitives.arrays.NewPrimFactory;
import som.primitives.arrays.PutAllNodeFactory;
import som.primitives.arrays.ToArgumentsArrayNodeGen;
import som.vm.NotYetImplementedException;
import som.vm.Universe;
import som.vm.constants.Classes;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.MateClasses;
import som.vmobjects.SArray;
import som.vmobjects.SBlock;
import som.vmobjects.SSymbol;
import tools.dym.Tags.VirtualInvoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;
import com.oracle.truffle.api.instrumentation.StandardTags.CallTag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.source.SourceSection;

public final class MessageSendNode {

  public static AbstractMessageSendNode create(final SSymbol selector,
      final ExpressionNode[] arguments, final SourceSection source) {
    return new UninitializedMessageSendNode(selector, arguments, source);
  }

  public static AbstractMessageSendNode createForPerformNodes(final SSymbol selector) {
    return new UninitializedSymbolSendNode(selector, null);
  }

  public static GenericMessageSendNode createGeneric(final SSymbol selector,
      final ExpressionNode[] argumentNodes, final SourceSection source) {
    if (Universe.getCurrent().vmReflectionEnabled()){
      return new MateGenericMessageSendNode(selector, argumentNodes,
          new UninitializedDispatchNode(source, selector), source);
    } else {
      return new GenericMessageSendNode(selector, argumentNodes,
          new UninitializedDispatchNode(source, selector), source);
    }  
  }

  public abstract static class AbstractMessageSendNode extends ExpressionWithTagsNode
      implements PreevaluatedExpression, ExpressionWithReceiver {

    public static AbstractMessageSpecializationsFactory specializationFactory = new AbstractMessageSpecializationsFactory.SOMMessageSpecializationsFactory();
    @Children protected final ExpressionNode[] argumentNodes;

    protected AbstractMessageSendNode(final ExpressionNode[] arguments,
        final SourceSection source) {
      super(source);
      this.argumentNodes = arguments;
    }

    public boolean isSuperSend() {
      return argumentNodes[0] instanceof ISuperReadNode;
    }
    
    @Override
    public ExpressionNode getReceiver() {
      return argumentNodes[0];
    }
    
    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      Object[] arguments = evaluateArguments(frame);
      return doPreEvaluated(frame, arguments);
    }
    
    public Object[] evaluateArguments(final VirtualFrame frame) {
      Object receiver = argumentNodes[0].executeGeneric(frame);
      return evaluateArgumentsWithReceiver(frame, receiver);
    }
    
    @Override
    public Object executeGenericWithReceiver(final VirtualFrame frame, final Object receiver) {
      Object[] arguments = evaluateArgumentsWithReceiver(frame, receiver);
      return doPreEvaluated(frame, arguments);
    }

    @ExplodeLoop
    public Object[] evaluateArgumentsWithReceiver(final VirtualFrame frame, final Object receiver) {
      Object[] arguments = new Object[argumentNodes.length];
      arguments[0] = receiver;
      for (int i = 1; i < argumentNodes.length; i++) {
        arguments[i] = argumentNodes[i].executeGeneric(frame);
        assert arguments[i] != null;
      }
      return arguments;
    }

    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == CallTag.class) {
        return true;
      }
      return super.isTaggedWith(tag);
    }
  }

  public abstract static class AbstractUninitializedMessageSendNode
      extends AbstractMessageSendNode {

    protected final SSymbol selector;
    
    public SSymbol getSelector(){
      return this.selector;
    }

    protected AbstractUninitializedMessageSendNode(final SSymbol selector,
        final ExpressionNode[] arguments, final SourceSection source) {
      super(arguments, source);
      this.selector = selector;
    }

    @Override
    public final Object doPreEvaluated(final VirtualFrame frame,
        final Object[] arguments) {
      return specialize(arguments, SArguments.getExecutionLevel(frame)).
          doPreEvaluated(frame, arguments);
    }

    private PreevaluatedExpression specialize(final Object[] arguments, ExecutionLevel level) {
      TruffleCompiler.transferToInterpreterAndInvalidate("Specialize Message Node");

      // first option is a super send, super sends are treated specially because
      // the receiver class is lexically determined
      if (isSuperSend()) {
        return makeSuperSend();
      }

      // We treat super sends separately for simplicity, might not be the
      // optimal solution, especially in cases were the knowledge of the
      // receiver class also allows us to do more specific things, but for the
      // moment  we will leave it at this.
      // TODO: revisit, and also do more specific optimizations for super sends.


      // let's organize the specializations by number of arguments
      // perhaps not the best, but one simple way to just get some order into
      // the chaos.

      switch (argumentNodes.length) {
        case  1: return specializeUnary(arguments, level);
        case  2: return specializeBinary(arguments, level);
        case  3: return specializeTernary(arguments, level);
        case  4: return specializeQuaternary(arguments, level);
      }

      return replace(makeGenericSend());
    }

    protected abstract PreevaluatedExpression makeSuperSend();

    protected GenericMessageSendNode makeGenericSend() {
      return new GenericMessageSendNode(selector,
          argumentNodes,
          new UninitializedDispatchNode(this.sourceSection, selector),
          getSourceSection());
    }
    
    protected <T extends EagerPrimitive> T makeEagerPrim(T prim) {
      Universe.insertInstrumentationWrapper(this);
      replace(prim);
      Universe.insertInstrumentationWrapper(prim);
      for (ExpressionNode arg: argumentNodes){
        SOMNode.unwrapIfNecessary(arg).markAsPrimitiveArgument();
        Universe.insertInstrumentationWrapper(arg);
      }
      return prim;
    }
    
    protected PreevaluatedExpression specializeUnary(final Object[] args, ExecutionLevel level) {
      Object receiver = args[0];
      switch (selector.getString()) {
        // eagerly but cautious:
        case "length":
          if (receiver instanceof SArray) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.unaryPrimitiveFor(selector,
                argumentNodes[0], LengthPrimFactory.create(getSourceSection(), null)));
          }
          break;
        case "value":
          if (receiver instanceof SBlock || receiver instanceof Boolean) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.unaryPrimitiveFor(selector,
                argumentNodes[0], ValueNonePrimFactory.create(getSourceSection(), null)));
          }
          break;
        case "not":
          if (receiver instanceof Boolean) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.unaryPrimitiveFor(selector,
                argumentNodes[0], NotMessageNodeFactory.create(getSourceSection(), null)));
          }
          break;
        case "abs":
          if (receiver instanceof Long) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.unaryPrimitiveFor(selector,
                argumentNodes[0], AbsPrimFactory.create(null)));
          }
      }
      return replace(makeGenericSend());
    }

    protected PreevaluatedExpression specializeBinary(final Object[] arguments, ExecutionLevel level) {
      switch (selector.getString()) {
        case "at:":
          if (arguments[0] instanceof SArray) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                AtPrimFactory.create(null, null)));
          }
          break;
        case "new:":
          if (arguments[0] == Classes.arrayClass) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                NewPrimFactory.create(null, null)));
          }
          break;
        case "instVarAt:":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector,
              argumentNodes[0], argumentNodes[1],
              InstVarAtPrimFactory.create(null, null)));
        case "doIndexes:":
          if (arguments[0] instanceof SArray) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                DoIndexesPrimFactory.create(getSourceSection(), null, null)));
          }
          break;
        case "do:":
          if (arguments[0] instanceof SArray) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                DoPrimFactory.create(getSourceSection(), null, null)));
          }
          break;
        case "putAll:":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector,
                argumentNodes[0], argumentNodes[1],
                PutAllNodeFactory.create(getSourceSection(), null, null, LengthPrimFactory.create(getSourceSection(), null))));
        case "whileTrue:": {
          if (argumentNodes[1] instanceof BlockNode &&
              argumentNodes[0] instanceof BlockNode) {
            BlockNode argBlockNode = (BlockNode) argumentNodes[1];
            SBlock    argBlock     = (SBlock)    arguments[1];
            return replace(
                AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                    argumentNodes[1],
                    new WhileTrueStaticBlocksNode(
                        (BlockNode) argumentNodes[0], argBlockNode,
                        (SBlock) arguments[0],
                        argBlock, getSourceSection(), level)));
          }
          break; // use normal send
        }
        case "whileFalse:":
          if (argumentNodes[1] instanceof BlockNode &&
              argumentNodes[0] instanceof BlockNode) {
            BlockNode argBlockNode = (BlockNode) argumentNodes[1];
            SBlock    argBlock     = (SBlock)    arguments[1];
            return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                new WhileFalseStaticBlocksNode(
                    (BlockNode) argumentNodes[0], argBlockNode,
                    (SBlock) arguments[0], argBlock, getSourceSection(), level)));
          }
          break; // use normal send
        case "and:":
        case "&&":
          if (arguments[0] instanceof Boolean) {
            if (argumentNodes[1] instanceof BlockNode) {
              return replace(AndMessageNodeFactory.create((SBlock) arguments[1],
                  getSourceSection(), level, argumentNodes[0], argumentNodes[1]));
            } else if (arguments[1] instanceof Boolean) {
              return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                  argumentNodes[1],
                  AndBoolMessageNodeFactory.create(
                      getSourceSection(),
                      argumentNodes[0], argumentNodes[1])));
            }
          }
          break;
        case "or:":
        case "||":
          if (arguments[0] instanceof Boolean) {
            if (argumentNodes[1] instanceof BlockNode) {
              return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                  argumentNodes[1],
                  OrMessageNodeGen.create((SBlock) arguments[1],
                      getSourceSection(), level,
                      argumentNodes[0], argumentNodes[1])));
            } else if (arguments[1] instanceof Boolean) {
              return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                  argumentNodes[1],
                  OrBoolMessageNodeGen.create(
                      getSourceSection(),
                      argumentNodes[0], argumentNodes[1])));
            }
          }
          break;

        case "value:":
          if (arguments[0] instanceof SBlock) {
            return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                ValueOnePrimFactory.create(getSourceSection(), null, null)));
          }
          break;

        case "ifTrue:":
          return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              IfMessageNodeGen.create(true, getSourceSection(),
                  argumentNodes[0], argumentNodes[1])));
        case "ifFalse:":
          return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              IfMessageNodeGen.create(false, getSourceSection(),
                  argumentNodes[0], argumentNodes[1])));
        case "to:":
          if (arguments[0] instanceof Long) {
            return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                ToPrimFactory.create(null, null)));
          }
          break;

        // TODO: find a better way for primitives, use annotation or something
        case "<":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              LessThanPrimFactory.create(null, null)));
        case "<=":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1], LessThanOrEqualPrimFactory.create(null, null)));
        case ">":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              GreaterThanPrimFactory.create(null, null)));
        case "+":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              AdditionPrimFactory.create(null, null)));
        case "-":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              SubtractionPrimFactory.create(null, null)));
        case "*":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              MultiplicationPrimFactory.create(null, null)));
        case "=":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              EqualsPrimFactory.create(null, null)));
        case "<>":
          return replace(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              UnequalsPrimFactory.create(null, null)));
        case "~=":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              UnequalsPrimFactory.create(null, null)));
        case "==":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              EqualsEqualsPrimFactory.create(null, null)));
        case "bitXor:":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              BitXorPrimFactory.create(null, null)));
        case "//":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              DoubleDivPrimFactory.create(null, null)));
        case "%":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              ModuloPrimFactory.create(null, null)));
        case "rem:":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              RemainderPrimFactory.create(null, null)));
        case "/":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              DividePrimFactory.create(null, null)));
        case "&":
          return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1],
              LogicAndPrimFactory.create(null, null)));

        // eagerly but cautious:
        case "<<":
          if (arguments[0] instanceof Long) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                LeftShiftPrimFactory.create(null, null)));
          }
          break;
        case ">>>":
          if (arguments[0] instanceof Long) {
            return makeEagerPrim(AbstractMessageSendNode.specializationFactory.binaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1],
                UnsignedRightShiftPrimFactory.create(null, null)));
          }
          break;

      }

      return replace(makeGenericSend());
    }

    protected PreevaluatedExpression specializeTernary(final Object[] arguments, ExecutionLevel level) {
      switch (selector.getString()) {
        case "at:put:":
          if (arguments[0] instanceof SArray) {
            return replace(AbstractMessageSendNode.specializationFactory.ternaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1], argumentNodes[2],
                AtPutPrimFactory.create(null, null, null)));
          }
          break;
        case "ifTrue:ifFalse:":
          return replace(AbstractMessageSendNode.specializationFactory.ternaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1], argumentNodes[2],
              IfTrueIfFalseMessageNodeGen.create(arguments[0],
                  arguments[1], arguments[2], level, argumentNodes[0],
                  argumentNodes[1], argumentNodes[2])));
        case "to:do:":
          if (TypesGen.isLong(arguments[0]) &&
              (TypesGen.isLong(arguments[1]) ||
                  TypesGen.isDouble(arguments[1])) &&
              TypesGen.isSBlock(arguments[2])) {
            return replace(AbstractMessageSendNode.specializationFactory.ternaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1], argumentNodes[2],
                IntToDoMessageNodeGen.create(this,
                    (SBlock) arguments[2], level, argumentNodes[0], argumentNodes[1],
                    argumentNodes[2])));
          }
          break;
        case "downTo:do:":
          if (TypesGen.isLong(arguments[0]) &&
              (TypesGen.isLong(arguments[1]) ||
                  TypesGen.isDouble(arguments[1])) &&
              TypesGen.isSBlock(arguments[2])) {
            return replace(AbstractMessageSendNode.specializationFactory.ternaryPrimitiveFor(selector, argumentNodes[0],
                argumentNodes[1], argumentNodes[2],
                IntDownToDoMessageNodeGen.create(this,
                    (SBlock) arguments[2], level, argumentNodes[0], argumentNodes[1],
                    argumentNodes[2])));
          }
          break;

        case "invokeOn:with:":
          return replace(InvokeOnPrimFactory.create(
              argumentNodes[0], argumentNodes[1], argumentNodes[2],
              ToArgumentsArrayNodeGen.create(null, null)));
        case "instVarAt:put:":
          return replace(AbstractMessageSendNode.specializationFactory.ternaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1], argumentNodes[2],
              InstVarAtPutPrimFactory.create(
                  argumentNodes[0], argumentNodes[1], argumentNodes[2])));
      }
      return replace(makeGenericSend());
    }

    protected PreevaluatedExpression specializeQuaternary(
        final Object[] arguments, ExecutionLevel level) {
      switch (selector.getString()) {
        case "to:by:do:":
          return replace(AbstractMessageSendNode.specializationFactory.quaternaryPrimitiveFor(selector, argumentNodes[0],
              argumentNodes[1], argumentNodes[2], argumentNodes[3],
              IntToByDoMessageNodeGen.create(this,
                  (SBlock) arguments[3], level, argumentNodes[0], argumentNodes[1],
                  argumentNodes[2], argumentNodes[3])));
      }
      return replace(makeGenericSend());
    }
  }

  @Instrumentable(factory = MessageSendNodeWrapper.class)
  public static class UninitializedMessageSendNode
      extends AbstractUninitializedMessageSendNode implements PreevaluatedExpression{

    protected UninitializedMessageSendNode(final SSymbol selector,
        final ExpressionNode[] arguments, final SourceSection source) {
      super(selector, arguments, source);
    }

    @Override
    protected PreevaluatedExpression makeSuperSend() {
      ISuperReadNode argumentNode;
      argumentNode = (ISuperReadNode)(argumentNodes[0]);
      GenericMessageSendNode node = new GenericMessageSendNode(selector,
        argumentNodes, SuperDispatchNode.create(this.sourceSection, selector,
            argumentNode), getSourceSection());
      return replace(node);
    }
    
    protected UninitializedMessageSendNode(final UninitializedMessageSendNode wrappedNode) {
      super(wrappedNode.selector, null, null);
    }

    @Override
    public Node asMateNode() {
      return new MateUninitializedMessageSendNode(this);
    }
  }

  private static final class UninitializedSymbolSendNode
    extends AbstractUninitializedMessageSendNode {

    protected UninitializedSymbolSendNode(final SSymbol selector,
        final SourceSection source) {
      super(selector, new ExpressionNode[0], source);
    }

    @Override
    public boolean isSuperSend() {
      // TODO: is is correct?
      return false;
    }

    @Override
    protected PreevaluatedExpression makeSuperSend() {
      // should never be reached with isSuperSend() returning always false
      throw new NotYetImplementedException();
    }

    @Override
    protected PreevaluatedExpression specializeBinary(final Object[] arguments, ExecutionLevel level) {
      switch (selector.getString()) {
        case "whileTrue:": {
          if (arguments[1] instanceof SBlock && arguments[0] instanceof SBlock) {
            SBlock argBlock = (SBlock) arguments[1];
            return replace(new WhileWithDynamicBlocksNode((SBlock) arguments[0],
                argBlock, true, getSourceSection(), level));
          }
          break;
        }
        case "whileFalse:":
          if (arguments[1] instanceof SBlock && arguments[0] instanceof SBlock) {
            SBlock    argBlock     = (SBlock)    arguments[1];
            return replace(new WhileWithDynamicBlocksNode(
                (SBlock) arguments[0], argBlock, false, getSourceSection(), level));
          }
          break; // use normal send
      }

      return super.specializeBinary(arguments, level);
    }
  }

/// TODO: currently, we do not only specialize the given stuff above, but also what has been classified as 'value' sends in the OMOP branch. Is that a problem?

  @Instrumentable(factory = MessageSendNodeWrapper.class)
  public static class GenericMessageSendNode
      extends AbstractMessageSendNode {

    protected final SSymbol selector;

    @Child private AbstractDispatchNode dispatchNode;

    protected GenericMessageSendNode(final SSymbol selector,
        final ExpressionNode[] arguments,
        final AbstractDispatchNode dispatchNode, final SourceSection source) {
      super(arguments, source);
      this.selector = selector;
      this.dispatchNode = dispatchNode;
      this.adoptChildren();
    }

    @Override
    public Object doPreEvaluated(final VirtualFrame frame,
        final Object[] arguments) {
      return dispatchNode.executeDispatch(frame, MateClasses.STANDARD_ENVIRONMENT, SArguments.getExecutionLevel(frame), arguments);
    }

    public void replaceDispatchListHead(
        final GenericDispatchNode replacement) {
      CompilerAsserts.neverPartOfCompilation("GenericMessageSendNode.replaceDispatchListHead");
      dispatchNode.replace(replacement);
    }

    public AbstractDispatchNode getDispatchListHead() {
      return dispatchNode;
    }

    @Override
    public String toString() {
      return "GMsgSend(" + selector.getString() + ")";
    }

    @Override
    public NodeCost getCost() {
      return Cost.getCost(dispatchNode);
    }

    public SSymbol getSelector(){
      return this.selector;
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == VirtualInvoke.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }
  
  public static class CascadeMessageSendNode
      extends ExpressionWithTagsNode {
    @Child private ExpressionNode receiver;
    final @Children private ExpressionWithReceiver[] messages;
    
    public CascadeMessageSendNode(final ExpressionNode receiver,
        final ExpressionWithReceiver[] messages, final SourceSection source) {
    
      super(source);
      this.receiver = receiver;
      this.messages = messages;
    }
    
    @Override
    @ExplodeLoop
    public Object executeGeneric(final VirtualFrame frame) {
      Object rcvr = receiver.executeGeneric(frame);
    
      for (int i = 0; i < messages.length - 1; i++) {
        this.messages[i].executeGenericWithReceiver(frame, rcvr);
      }
    
      return this.messages[messages.length - 1].executeGenericWithReceiver(frame, rcvr);
    }
  }
}