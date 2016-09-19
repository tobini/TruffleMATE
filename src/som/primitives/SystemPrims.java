package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;
import som.vm.constants.Globals;
import som.vm.constants.Nil;
import som.vmobjects.SClass;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


public final class SystemPrims {
  public static final boolean receiverIsSystemObject(final DynamicObject receiver) {
    return receiver == Globals.systemObject;
  }

  @GenerateNodeFactory
  public abstract static class BinarySystemNode extends BinaryExpressionNode {
    protected final Universe universe;
    protected BinarySystemNode() { super(null); this.universe = Universe.getCurrent(); }
  }

  @ImportStatic(SystemPrims.class)
  public abstract static class LoadPrim extends BinarySystemNode {
    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final SSymbol argument) {
      DynamicObject result = universe.loadClass(argument);
      return result != null ? result : Nil.nilObject;
    }
  }

  @ImportStatic(SystemPrims.class)
  public abstract static class ExitPrim extends BinarySystemNode {
    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final long error) {
      universe.exit((int) error);
      return receiver;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  public abstract static class GlobalPutPrim extends TernaryExpressionNode {
    private final Universe universe;
    public GlobalPutPrim()  { this.universe = Universe.getCurrent(); }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final SSymbol global,
        final DynamicObject value) {
      universe.setGlobal(global, value);
      return value;
    }
  }

  @ImportStatic(SystemPrims.class)
  public abstract static class PrintStringPrim extends BinarySystemNode {
    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final String argument) {
      Universe.print(argument);
      return receiver;
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final SSymbol argument) {
      return doSObject(receiver, argument.getString());
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  public abstract static class PrintNewlinePrim extends UnaryExpressionNode {
    public PrintNewlinePrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Print New Line"));
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver) {
      Universe.println("");
      return receiver;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  public abstract static class FullGCPrim extends UnaryExpressionNode {
    public FullGCPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Full GC"));
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver) {
      System.gc();
      return true;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  public abstract static class TimePrim extends UnaryExpressionNode {
    public TimePrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Time"));
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final long doSObject(final DynamicObject receiver) {
      return System.currentTimeMillis() - startTime;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  public abstract static class TicksPrim extends UnaryExpressionNode {
    public TicksPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Ticks"));
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final long doSObject(final DynamicObject receiver) {
      return System.nanoTime() / 1000L - startMicroTime;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class CurrentInstancePrim extends UnaryExpressionNode {
    public CurrentInstancePrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Current system instance"));
    }

    @Specialization
    public final DynamicObject doSObject(final DynamicObject receiver) {
      assert(SClass.getName(receiver).equals("system"));
      return Universe.getCurrent().getSystemObject();
    }
  }
  
  {
    startMicroTime = System.nanoTime() / 1000L;
    startTime = startMicroTime / 1000L;
  }
  private static long startTime;
  private static long startMicroTime;
}
