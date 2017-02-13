package som.interpreter.nodes.specialized;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.Primitive;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
@Primitive(klass = "Boolean", selector = "not", receiverType = Boolean.class)
public abstract class NotMessageNode extends UnaryExpressionNode {
  public NotMessageNode(final boolean eagWrap, final SourceSection source) {
    super(eagWrap, source);
  }

  @Specialization
  public final boolean doNot(final VirtualFrame frame, final boolean receiver) {
    return !receiver;
  }
}
