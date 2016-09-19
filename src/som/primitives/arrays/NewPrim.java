package som.primitives.arrays;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.vm.constants.Classes;
import som.vmobjects.SArray;
import som.vmobjects.SClass;
import tools.dym.Tags.NewArray;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;


@GenerateNodeFactory
public abstract class NewPrim extends BinaryExpressionNode {
  public NewPrim() { 
    super(Source.newBuilder("new").internal().name("new primitive").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
  }
  
  protected static final boolean receiverIsArrayClass(final DynamicObject receiver) {
    return receiver == Classes.arrayClass;
  }
  
  protected static final boolean receiverIsByteArrayClass(final DynamicObject receiver) {
    return (SClass.getName(receiver).getString() == "ByteArray");
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
  protected boolean isTaggedWith(final Class<?> tag) {
    if (tag == NewArray.class) {
      return true;
    } else {
      return super.isTaggedWith(tag);
    }
  }
}
