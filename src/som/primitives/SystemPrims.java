package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;
import som.vm.constants.Globals;
import som.vm.constants.Nil;
import som.vmobjects.SBlock;
import som.vmobjects.SClass;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.impl.FindContextNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


public final class SystemPrims {
  public static final boolean receiverIsSystemObject(final DynamicObject receiver) {
    return receiver == Globals.systemObject;
  }

  @GenerateNodeFactory
  public abstract static class BinarySystemNode extends BinaryExpressionNode {
    protected final Universe universe;
    protected BinarySystemNode(final boolean eagWrap, final SourceSection source) { 
      super(eagWrap, source); 
      this.universe = Universe.getCurrent(); 
    }
  }

  @ImportStatic(SystemPrims.class)
  @Primitive(klass = "System", selector = "load:", eagerSpecializable = false)
  public abstract static class LoadPrim extends BinarySystemNode {
    protected LoadPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)", assumptions="cachedAssumption")
    public final Object doSObject(final DynamicObject receiver, final SSymbol argument,
        @Cached("currentUniverse()") final Universe currentUniverse,
        @Cached("getAssumption(currentUniverse)") final Assumption cachedAssumption) {
      DynamicObject result = currentUniverse.loadClass(argument);
      return result != null ? result : Nil.nilObject;
    }
    
    /*This may be the best to do for all system primitives since the universe may change.
     * It is mandatory for this primitive because the classpath changes, for instance, between test classes 
     */
    public static Universe currentUniverse(){
      return Universe.getCurrent();
    }
    
    public static Assumption getAssumption(Universe uni){
      return uni.getValidUniverseAssumption();
    }
  }

  @ImportStatic(SystemPrims.class)
  @Primitive(klass = "System", selector = "exit:", eagerSpecializable = false)
  public abstract static class ExitPrim extends BinarySystemNode {
    protected ExitPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final long error) {
      universe.exit((int) error);
      return receiver;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  @Primitive(klass = "System", selector = "global:put:", eagerSpecializable = false)
  public abstract static class GlobalPutPrim extends TernaryExpressionNode {
    private final Universe universe;
    public GlobalPutPrim(final boolean eagWrap, final SourceSection source)  { 
      super(false, source);
      this.universe = Universe.getCurrent();
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver, final SSymbol global,
        final DynamicObject value) {
      universe.setGlobal(global, value);
      return value;
    }
  }

  @ImportStatic(SystemPrims.class)
  @Primitive(klass = "System", selector = "printString:", eagerSpecializable = false)
  public abstract static class PrintStringPrim extends BinarySystemNode {
    protected PrintStringPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

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
  @Primitive(klass = "System", selector = "printNewline", eagerSpecializable = false)
  public abstract static class PrintNewlinePrim extends UnaryExpressionNode {
    public PrintNewlinePrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver) {
      Universe.println("");
      return receiver;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  @Primitive(klass = "System", selector = "fullGC", eagerSpecializable = false)
  public abstract static class FullGCPrim extends UnaryExpressionNode {
    public FullGCPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final Object doSObject(final DynamicObject receiver) {
      System.gc();
      return true;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  @Primitive(klass = "System", selector = "time", eagerSpecializable = false)
  public abstract static class TimePrim extends UnaryExpressionNode {
    public TimePrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final long doSObject(final DynamicObject receiver) {
      return System.currentTimeMillis() - startTime;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  @Primitive(klass = "System", selector = "ticks", eagerSpecializable = false)
  public abstract static class TicksPrim extends UnaryExpressionNode {
    public TicksPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final long doSObject(final DynamicObject receiver) {
      return System.nanoTime() / 1000L - startMicroTime;
    }
  }

  @ImportStatic(SystemPrims.class)
  @GenerateNodeFactory
  @Primitive(klass = "System", selector = "export:as:", eagerSpecializable = false)
  public abstract static class ExportAsPrim extends TernaryExpressionNode {
    @Child protected FindContextNode<Universe> findContext;

    public ExportAsPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
      findContext = SomLanguage.INSTANCE.createNewFindContextNode();
    }

    @Specialization(guards = "receiverIsSystemObject(obj)")
    public final boolean doString(final DynamicObject obj, final SBlock method, final String name) {
      Universe vm = findContext.executeFindContext();
      vm.registerExport(name, method);
      return true;
    }

    @Specialization(guards = "receiverIsSystemObject(obj)")
    public final boolean doSymbol(final DynamicObject obj, final SBlock method, final SSymbol name) {
      return doString(obj, method, name.getString());
    }
  }
  
  @GenerateNodeFactory
  @Primitive(klass = "System Class", selector = "current", eagerSpecializable = false)
  public abstract static class CurrentInstancePrim extends UnaryExpressionNode {
    public CurrentInstancePrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
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
