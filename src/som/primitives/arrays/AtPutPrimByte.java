package som.primitives.arrays;

import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.vm.constants.Nil;
import som.vmobjects.SArray;
import som.vmobjects.SArray.ArrayType;
import som.vmobjects.SArray.PartiallyEmptyArray;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ValueProfile;


@GenerateNodeFactory
@ImportStatic(ArrayType.class)
public abstract class AtPutPrimByte extends TernaryExpressionNode {

  private final ValueProfile storageType = ValueProfile.createClassProfile();

  @Specialization(guards = {"isEmptyType(receiver)"})
  public final long doEmptySArray(final SArray receiver, final long index,
      final long value) {
    long idx = index - 1;
    assert idx >= 0;
    assert idx < receiver.getEmptyStorage(storageType);

    receiver.transitionFromEmptyToPartiallyEmptyWith(idx, (byte)value);
    return value;
  }

  private void setValue(final long idx, final Object value,
      final PartiallyEmptyArray storage) {
    assert idx >= 0;
    assert idx < storage.getLength();

    if (storage.get(idx) == Nil.nilObject) {
      storage.decEmptyElements();
    }
    storage.set(idx, value);
  }

  @Specialization(guards = "isPartiallyEmptyType(receiver)")
  public final long doPartiallyEmptySArray(final SArray receiver,
      final long index, final long value) {
    long idx = index - 1;
    PartiallyEmptyArray storage = receiver.getPartiallyEmptyStorage(storageType);
    setValue(idx, (byte)value, storage);
    if (storage.getType() != ArrayType.BYTE) {
      storage.setType(ArrayType.BYTE);
    }
    receiver.ifFullTransitionPartiallyEmpty();
    return value;
  }

  
  @Specialization
  public final Object doObjectSArray(final SArray receiver, final long index,
      long value) {
    long idx = index - 1;
    receiver.getObjectStorage(storageType)[(int) idx] = (byte)value;
    return value;
  }
}
