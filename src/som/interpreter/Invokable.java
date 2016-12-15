package som.interpreter;

import som.compiler.MethodGenerationContext;
import som.compiler.Variable.Local;
import som.interpreter.nodes.MateReturnNode;
import som.interpreter.nodes.nary.ExpressionWithTagsNode;
import som.vm.Universe;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public abstract class Invokable extends RootNode implements MateNode{

  @Child protected ExpressionWithTagsNode expressionOrSequence;
  
  protected final ExpressionWithTagsNode uninitializedBody;
  @CompilationFinal protected DynamicObject belongsToMethod;

  public Invokable(final SourceSection sourceSection,
      final FrameDescriptor frameDescriptor,
      final ExpressionWithTagsNode expressionOrSequence,
      final ExpressionWithTagsNode uninitialized,
      DynamicObject method) {
    super(SomLanguage.class, sourceSection, frameDescriptor);
    this.uninitializedBody = this.mateifyUninitializedNode(uninitialized);
    this.expressionOrSequence = expressionOrSequence;
    this.belongsToMethod = method;
  }

  @Override
  public final Object execute(final VirtualFrame frame) {
    return expressionOrSequence.executeGeneric(frame);
  }

  public abstract Invokable cloneWithNewLexicalContext(final LexicalScope outerContext);
  public ExpressionWithTagsNode inline(final MethodGenerationContext mgenc,
      final Local[] locals) {
    return InlinerForLexicallyEmbeddedMethods.doInline(uninitializedBody, mgenc,
        locals, getSourceSection().getCharIndex());
  }

  @Override
  public final boolean isCloningAllowed() {
    return true;
  }
  
  @Override
  public ExecutionContext getExecutionContext() {
    return Universe.getCurrent();
  }
  
  public DynamicObject getBelongsToMethod() {
    return this.belongsToMethod; 
  }
  
  public final RootCallTarget createCallTarget() {
    return Truffle.getRuntime().createCallTarget(this);
  }

  public abstract void propagateLoopCountThroughoutLexicalScope(final long count);
  
  public void wrapIntoMateNode() {
    if (this.asMateNode() != null) this.replace(this.asMateNode());
    if (!(this.expressionOrSequence instanceof MateReturnNode)){
      this.expressionOrSequence.replace(new MateReturnNode(this.expressionOrSequence));
      uninitializedBody.accept(new MateifyVisitor());
    }
  }
  
  private ExpressionWithTagsNode mateifyUninitializedNode(ExpressionWithTagsNode uninitialized){
    if (!(Universe.getCurrent().vmReflectionEnabled()) || uninitialized.asMateNode() == null) {
        return uninitialized;
    } else {
      return (ExpressionWithTagsNode)uninitialized.asMateNode();
    }
  }
  
  public void setMethod(DynamicObject method) {
    this.belongsToMethod = method;
  }
  
  @Override
  protected boolean isTaggedWith(final Class<?> tag) {
    if (tag == RootTag.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
