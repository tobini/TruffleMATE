package som.primitives.arrays;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.primitives.Primitive;
import som.primitives.Primitives.Specializer;
import som.vm.constants.Classes;
import som.vmobjects.SArray;
import som.vmobjects.SClass;
import tools.dym.Tags.NewArray;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@GenerateNodeFactory
@Primitive(klass = "Array class", selector = "new:",
           specializer = NewPrim.IsArrayClass.class)
public abstract class NewPrim extends BinaryExpressionNode {
  public NewPrim(final boolean eagWrap, final SourceSection source) {
    super(eagWrap, source);
  }

  public static class IsArrayClass extends Specializer<NewPrim> {
    public IsArrayClass(final Primitive prim, final NodeFactory<NewPrim> fact) { super(prim, fact); }

    @Override
    public boolean matches(final Object[] args, final ExpressionNode[] argNodes) {
      return receiverIsArrayClass((DynamicObject) args[0]);
    }
  }

  protected static final boolean receiverIsArrayClass(final DynamicObject receiver) {
    return receiver == Classes.arrayClass;
  }

  protected static final boolean receiverIsByteArrayClass(final DynamicObject receiver) {
    return (SClass.getName(receiver).getString().equals("ByteArray"));
  }

  @Specialization(guards = "receiverIsArrayClass(receiver)")
  public final SArray doSClass(final DynamicObject receiver, final long length) {
    return new SArray(length);
  }

  @Specialization(guards = "receiverIsByteArrayClass(receiver)")
  public final SArray doByteSClass(final DynamicObject receiver, final long length) {
    return SArray.create(new byte[(int) length]);
  }

  @Override
  protected boolean isTaggedWithIgnoringEagerness(final Class<?> tag) {
    if (tag == NewArray.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
