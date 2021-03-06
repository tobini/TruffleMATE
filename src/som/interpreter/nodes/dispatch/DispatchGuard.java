package som.interpreter.nodes.dispatch;

import som.vmobjects.SBlock;
import som.vmobjects.SObjectLayoutImpl.SObjectType;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;


public abstract class DispatchGuard {
  public abstract boolean entryMatches(Object obj) throws InvalidAssumptionException;

  public static DispatchGuard create(final Object obj) {
    if (obj == Boolean.TRUE) {
      return new CheckTrue();
    }

    if (obj == Boolean.FALSE) {
      return new CheckFalse();
    }

    if (obj instanceof DynamicObject) {
      return new CheckSObject(((DynamicObject) obj).getShape());
    }

    if (obj instanceof SBlock) {
      return new CheckSBlock(((SBlock) obj).getSOMClass());
    }

    return new CheckClass(obj.getClass());
  }

  public static DispatchGuard createForBlock(final SBlock block) {
    return new BlockMethod(block.getMethod());
  }

  private static final class BlockMethod extends DispatchGuard {
    private final DynamicObject expected;

    BlockMethod(final DynamicObject method) {
      this.expected = method;
    }

    @Override
    public boolean entryMatches(final Object obj) throws InvalidAssumptionException {
      return ((SBlock) obj).getMethod() == expected;
    }
  }

  private static final class CheckClass extends DispatchGuard {

    private final Class<?> expected;

    CheckClass(final Class<?> expectedClass) {
      this.expected = expectedClass;
    }

    @Override
    public boolean entryMatches(final Object obj) throws InvalidAssumptionException {
      return obj.getClass() == expected;
    }
  }

  private static final class CheckTrue extends DispatchGuard {
    @Override
    public boolean entryMatches(final Object obj) throws InvalidAssumptionException {
      return obj == Boolean.TRUE;
    }
  }

  private static final class CheckFalse extends DispatchGuard {
    @Override
    public boolean entryMatches(final Object obj) throws InvalidAssumptionException {
      return obj == Boolean.FALSE;
    }
  }

  private static final class CheckSObject extends DispatchGuard {
    private final Shape expected;
    private final DynamicObject klass;

    CheckSObject(final Shape expected) {
      this.expected = expected;
      this.klass = ((SObjectType) (expected.getObjectType())).getKlass();
    }

  @Override
  public boolean entryMatches(final Object obj) throws InvalidAssumptionException {
    if (!expected.isValid()) {
      CompilerDirectives.transferToInterpreter();
      throw new InvalidAssumptionException();
    }
    return obj instanceof DynamicObject &&
        (
            (((DynamicObject) obj).getShape() == expected) ||
            ((((SObjectType) ((DynamicObject) obj).getShape().getObjectType())).getKlass() == klass)
        );
    }
  }

  private static final class CheckSBlock extends DispatchGuard {
    private final DynamicObject expected;

    CheckSBlock(final DynamicObject blockClass) {
      this.expected = blockClass;
    }

  @Override
  public boolean entryMatches(final Object obj) throws InvalidAssumptionException {

    return obj instanceof SBlock &&
        ((SBlock) obj).getSOMClass() == expected;
    }
  }
}
