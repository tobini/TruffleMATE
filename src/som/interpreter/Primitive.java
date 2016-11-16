package som.interpreter;

import som.interpreter.nodes.nary.ExpressionWithTagsNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;


public final class Primitive extends Invokable {

  public Primitive(final ExpressionWithTagsNode primitive,
      final FrameDescriptor frameDescriptor,
      final ExpressionWithTagsNode uninitialized,
      DynamicObject method) {
    super(null, frameDescriptor, primitive, uninitialized, method);
  }

  @Override
  public Invokable cloneWithNewLexicalContext(final LexicalScope outerContext) {
    assert outerContext == null;
    FrameDescriptor inlinedFrameDescriptor = getFrameDescriptor().copy();
    LexicalScope  inlinedContext = new LexicalScope(inlinedFrameDescriptor,
        outerContext);
    ExpressionWithTagsNode inlinedBody = SplitterForLexicallyEmbeddedCode.doInline(uninitializedBody,
        inlinedContext);
    return new Primitive(inlinedBody, inlinedFrameDescriptor, uninitializedBody, this.belongsToMethod);
  }

  @Override
  public Node deepCopy() {
    return cloneWithNewLexicalContext(null);
  }

  @Override
  public String toString() {
    return "Primitive " + expressionOrSequence.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
  }

  @Override
  public void propagateLoopCountThroughoutLexicalScope(final long count) {
    propagateLoopCount(count);
  }

  private static Method getNextMethodOnStack() {
    return Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<Method>() {
        @Override
        public Method visitFrame(final FrameInstance frameInstance) {
          RootCallTarget ct = (RootCallTarget) frameInstance.getCallTarget();
          Invokable m = (Invokable) ct.getRootNode();
          if (m instanceof Primitive) {
            return null;
          } else {
            return (Method) m;
          }
        }
      });
  }

  public static void propagateLoopCount(final long count) {
    CompilerAsserts.neverPartOfCompilation("Primitive.pLC(.)");

    // we need to skip the primitive and get to the method that called the primitive
    FrameInstance caller = Truffle.getRuntime().getCallerFrame();

    RootCallTarget ct = (RootCallTarget) caller.getCallTarget();  // caller method
    Invokable m = (Invokable) ct.getRootNode();

    if (m instanceof Primitive) {
      // the caller is a primitive, that doesn't help, we need to skip it and
      // find a proper method
      m = getNextMethodOnStack();
    }

    if (m != null && !(m instanceof Primitive)) {
      m.propagateLoopCountThroughoutLexicalScope(count);
    }
  }
  
  /**
   * Primitive operations are not instrumentable. They are not user-level
   * behavior, and thus, are supposed to remain opaque.
   */
  @Override
  protected boolean isInstrumentable() {
    return false;
  }
}
