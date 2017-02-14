package som.primitives.reflection;

import som.interpreter.SArguments;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.primitives.Primitive;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@Primitive(klass = "Object", selector = "perform:inSuperclass:")
public abstract class PerformInSuperclassPrim extends TernaryExpressionNode {
  public PerformInSuperclassPrim(final boolean eagWrap, final SourceSection source) {
    super(false, source);
  }

  @Child private IndirectCallNode call = Truffle.getRuntime().createIndirectCallNode();

  @Specialization
  public final Object doSAbstractObject(final VirtualFrame frame,
      final Object receiver, final SSymbol selector, final DynamicObject clazz) {
    CompilerAsserts.neverPartOfCompilation("PerformInSuperclassPrim");
    DynamicObject invokable = SClass.lookupInvokable(clazz, selector);
    return call.call(SInvokable.getCallTarget(invokable, SArguments.getExecutionLevel(frame)), new Object[] {SArguments.getEnvironment(frame), SArguments.getExecutionLevel(frame), receiver});
  }
}
