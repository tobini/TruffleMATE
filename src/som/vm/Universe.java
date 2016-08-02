/**
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.vm;

import static som.vm.constants.Classes.systemClass;

import java.io.IOException;
import java.util.Map;

import som.VMOptions;
import som.VmSettings;
import som.interpreter.Invokable;
import som.interpreter.MateifyVisitor;
import som.interpreter.SomLanguage;
import som.interpreter.TruffleCompiler;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.MateClasses;
import som.vmobjects.InvokableLayoutImpl;
import som.vmobjects.SArray;
import som.vmobjects.SBasicObjectLayoutImpl;
import som.vmobjects.SBlock;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SInvokable.SMethod;
import som.vmobjects.SInvokable.SPrimitive;
import som.vmobjects.SObject;
import som.vmobjects.SObjectLayoutImpl;
import som.vmobjects.SReflectiveObject;
import som.vmobjects.SReflectiveObjectLayoutImpl;
import som.vmobjects.SSymbol;
import tools.debugger.WebDebugger;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.ExecutionEvent;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.vm.EventConsumer;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Builder;
import com.oracle.truffle.api.vm.PolyglotEngine.Instrument;

public class Universe extends ExecutionContext {
  public Universe(final String[] args) throws IOException {
    current = this;
    this.truffleRuntime = Truffle.getRuntime();
    this.avoidExit    = false;
    this.lastExitCode = 0;
    options = new VMOptions(args);
    mateDeactivated = this.getTruffleRuntime().createAssumption();
    mateActivated = null;
    if (ObjectMemory.last == null){
      objectMemory = new ObjectMemory(options.classPath);
      objectMemory.initializeSystem();
    } else {
      objectMemory = ObjectMemory.last;
      objectMemory.setClassPath(options.classPath);    
    }
    if (options.showUsage) {
      VMOptions.printUsageAndExit();
    }
    if (options.vmReflectionActivated){
      activatedMate();
    }
  }
  
  public static Universe getInitializedVM(String[] arguments) throws IOException {
    Builder builder = PolyglotEngine.newBuilder();
    builder.config(SomLanguage.MIME_TYPE, SomLanguage.CMD_ARGS, arguments);
    PolyglotEngine engine = builder.build();

    engine.getInstruments().values().forEach(i -> i.setEnabled(false));

    // Trigger initialization
    assert null == engine.getLanguages().get(SomLanguage.MIME_TYPE).getGlobalObject();
    return Universe.getCurrent();
  }
  
  public static void main(final String[] args) {
    Builder builder = PolyglotEngine.newBuilder();
    builder.config(SomLanguage.MIME_TYPE, SomLanguage.CMD_ARGS, args);
    VMOptions vmOptions = new VMOptions(args);

    if (vmOptions.debuggerEnabled) {
      //startDebugger(builder);
    } else {
      startExecution(builder, vmOptions);
    }
  }
  
  private static void startExecution(final Builder builder,
      final VMOptions vmOptions) {
    if (vmOptions.webDebuggerEnabled) {
      builder.onEvent(onExec).onEvent(onHalted);
    }
    engine = builder.build();

    try {
      Map<String, Instrument> instruments = engine.getInstruments();
      Instrument profiler = instruments.get("profiler");
      if (vmOptions.profilingEnabled && profiler == null) {
        errorPrintln("Truffle profiler not available. Might be a class path issue");
      } else if (profiler != null) {
        profiler.setEnabled(vmOptions.profilingEnabled);
      }
      //instruments.get(Highlight.ID).setEnabled(vmOptions.highlightingEnabled);

      if (VmSettings.TRUFFLE_DEBUGGER_ENABLED) {
        debugger = Debugger.find(engine);
      }

      if (vmOptions.webDebuggerEnabled) {
        assert debugger != null;
        Instrument webDebuggerInst = instruments.get(WebDebugger.ID);
        webDebuggerInst.setEnabled(true);

        webDebugger = webDebuggerInst.lookup(WebDebugger.class);
        //webDebugger.startServer(debugger);
      }

      /*if (vmOptions.dynamicMetricsEnabled) {
        assert VmSettings.DYNAMIC_METRICS;
        Instrument dynM = instruments.get(DynamicMetrics.ID);
        dynM.setEnabled(true);
        structuralProbes = dynM.lookup(StructuralProbe.class);
        assert structuralProbes != null : "Initialization of DynamicMetrics tool incomplete";
      }*/

      //engine.eval(null);
      engine.eval(SomLanguage.START);
      engine.dispose();
    } catch (IOException e) {
      throw new RuntimeException("This should never happen", e);
    }
    System.exit(Universe.getCurrent().lastExitCode);
  }

  public Object execute(final String className, final String selector) {
    DynamicObject clazz = objectMemory.loadClass(symbolFor(className), null);

    // Lookup the initialize invokable on the system class
    DynamicObject initialize = SClass.lookupInvokable(SObject.getSOMClass(clazz),
        symbolFor(selector));
    return SInvokable.invoke(initialize, MateClasses.STANDARD_ENVIRONMENT, ExecutionLevel.Base, clazz);
  }

  public Object execute() {
    // Start the shell if no filename is given
    String[] arguments = options.args;
    if (arguments.length == 0) {
      Shell shell = new Shell(this);
      return shell.start();
    }

    // Lookup the initialize invokable on the system class
    DynamicObject initialize = SClass.lookupInvokable(
        systemClass, symbolFor("initialize:"));

    return SInvokable.invoke(initialize, MateClasses.STANDARD_ENVIRONMENT, ExecutionLevel.Base, objectMemory.getSystemObject(), SArray.create(arguments));
  }
  
  public void mateify(DynamicObject clazz) {
    int countOfInvokables = SClass.getNumberOfInstanceInvokables(clazz);
    MateifyVisitor visitor = new MateifyVisitor();
    for (int i = 0; i < countOfInvokables; i++){
      DynamicObject method = SClass.getInstanceInvokable(clazz, i);
      Invokable node = InvokableLayoutImpl.INSTANCE.getInvokable(method);
      node.accept(visitor);
    }
  }
  
  public void mateifyMethod(DynamicObject method) {
    MateifyVisitor visitor = new MateifyVisitor();
    Invokable node = InvokableLayoutImpl.INSTANCE.getInvokable(method);
    node.accept(visitor);
  }
  
  public TruffleRuntime getTruffleRuntime() {
    return truffleRuntime;
  }

  public void exit(final int errorCode) {
    TruffleCompiler.transferToInterpreter("exit");
    // Exit from the Java system
    if (!avoidExit) {
      System.exit(errorCode);
    } else {
      lastExitCode = errorCode;
    }
  }

  public int lastExitCode() {
    return lastExitCode;
  }

  public static void errorExit(final String message) {
    TruffleCompiler.transferToInterpreter("errorExit");
    errorPrintln("Runtime Error: " + message);
    Universe.getCurrent().exit(1);
  }

  @TruffleBoundary
  public SSymbol symbolFor(final String string) {
    return objectMemory.symbolFor(string);
  }

  public static SBlock newBlock(final DynamicObject method,
      final DynamicObject blockClass, final MaterializedFrame context) {
    return new SBlock(method, blockClass, context);
  }

  @TruffleBoundary
  public static DynamicObject newMethod(final SSymbol signature,
      final Invokable truffleInvokable, final boolean isPrimitive,
      final DynamicObject[] embeddedBlocks) {
    if (isPrimitive) {
      return SPrimitive.create(signature, truffleInvokable);
    } else {
      return SMethod.create(signature, truffleInvokable, embeddedBlocks);
    }
  }
  
  public DynamicObject loadClass(final SSymbol name) {
    return objectMemory.loadClass(name, null);
  }

  @TruffleBoundary
  public boolean hasGlobal(final SSymbol name) {
    return objectMemory.hasGlobal(name);
  }

  @TruffleBoundary
  public DynamicObject getGlobal(final SSymbol name) {
    return objectMemory.getGlobal(name);
  }

  public void setGlobal(final String name, final DynamicObject value) {
    objectMemory.setGlobal(symbolFor(name), value);
  }

  @TruffleBoundary
  public void setGlobal(final SSymbol name, final DynamicObject value) {
    objectMemory.setGlobal(name, value);
  }

  public DynamicObject getBlockClass(final int numberOfArguments) {
    return objectMemory.getBlockClass(numberOfArguments);
  }

  @TruffleBoundary
  public DynamicObject loadShellClass(final String stmt) throws IOException {
    return objectMemory.loadShellClass(stmt);
  }

  public void setAvoidExit(final boolean value) {
    avoidExit = value;
  }

  @TruffleBoundary
  public static void errorPrint(final String msg) {
    // Checkstyle: stop
    System.err.print(msg);
    // Checkstyle: resume
  }

  @TruffleBoundary
  public static void errorPrintln(final String msg) {
    // Checkstyle: stop
    System.err.println(msg);
    // Checkstyle: resume
  }

  @TruffleBoundary
  public static void errorPrintln() {
    // Checkstyle: stop
    System.err.println();
    // Checkstyle: resume
  }

  @TruffleBoundary
  public static void print(final String msg) {
    // Checkstyle: stop
    System.out.print(msg);
    // Checkstyle: resume
  }

  @TruffleBoundary
  public static void println(final String msg) {
    // Checkstyle: stop
    System.out.println(msg);
    // Checkstyle: resume
  }

  @TruffleBoundary
  public static void println() {
    // Checkstyle: stop
    System.out.println();
    // Checkstyle: resume
  }

  public static Universe getCurrent(){
    return current;
  }

  public static void setCurrent(final Universe universe){
    current = universe;
  }

  public DynamicObjectFactory getInstancesFactory(){
    if (options.vmReflectionEnabled){
      return SReflectiveObject.SREFLECTIVE_OBJECT_FACTORY;
    } else {
      return SObject.SOBJECT_FACTORY;
    }
  }
  
  public String imageName(){
    return "Smalltalk/fake.image";
  }
  
  public static String frameOnStackSlotName(){
    // Name for the frameOnStack slot,
    // starting with ! to make it a name that's not possible in Smalltalk
    return "!frameOnStack";
  }
  
  public DynamicObject createNilObject() {
    DynamicObject dummyObjectForInitialization = SBasicObjectLayoutImpl.INSTANCE.createSBasicObject();
    if (options.vmReflectionEnabled){
      return SReflectiveObjectLayoutImpl.INSTANCE.createSReflectiveObjectShape(dummyObjectForInitialization, dummyObjectForInitialization).newInstance();
    } else {
      return SObjectLayoutImpl.INSTANCE.createSObjectShape(dummyObjectForInitialization).newInstance();
    }
  }
  
  public DynamicObjectFactory createObjectShapeFactoryForClass(final DynamicObject clazz) {
    if (options.vmReflectionEnabled){
      return SReflectiveObject.createObjectShapeFactoryForClass(clazz);
    } else {
      return SObject.createObjectShapeFactoryForClass(clazz);
   }
  }
  
  public boolean vmReflectionEnabled(){
    return options.vmReflectionEnabled;
  }
  
  public boolean printAST(){
    return options.printAST;
  }
  
  public Assumption getMateDeactivatedAssumption(){
    return this.mateDeactivated;
  }
  
  public Assumption getMateActivatedAssumption(){
    return this.mateActivated;
  }
  
  public void activatedMate(){
    if (this.getMateDeactivatedAssumption().isValid()){
      this.getMateDeactivatedAssumption().invalidate();
    }
    mateActivated = this.getTruffleRuntime().createAssumption();
  }
  
  public void deactivateMate(){
    if (this.getMateActivatedAssumption().isValid()){
      this.getMateActivatedAssumption().invalidate();
    }
    mateDeactivated = this.getTruffleRuntime().createAssumption();
  }
  
  public DynamicObject getTrueObject()   { return objectMemory.getTrueObject(); }
  public DynamicObject getFalseObject()  { return objectMemory.getFalseObject(); }
  public DynamicObject getSystemObject() { return objectMemory.getSystemObject(); }
  
  private static final EventConsumer<ExecutionEvent> onExec =
      new EventConsumer<ExecutionEvent>(ExecutionEvent.class) {
    @Override
    protected void on(final ExecutionEvent event) {
      webDebugger.reportExecutionEvent(event);
    }
  };

  private static final EventConsumer<SuspendedEvent> onHalted =
      new EventConsumer<SuspendedEvent>(SuspendedEvent.class) {
    @Override
    protected void on(final SuspendedEvent e) {
      webDebugger.reportSuspendedEvent(e);
    }
  };
  
  private final TruffleRuntime                  truffleRuntime;
  // TODO: this is not how it is supposed to be... it is just a hack to cope
  //       with the use of system.exit in SOM to enable testing
  @CompilationFinal private boolean             avoidExit;
  private int                                   lastExitCode;

  // Latest instance
  // WARNING: this is problematic with multiple interpreters in the same VM...
  @CompilationFinal private static Universe current;
  @CompilationFinal private static PolyglotEngine engine;
  private final ObjectMemory objectMemory;
  
  @CompilationFinal private static Debugger    debugger;
  @CompilationFinal private static WebDebugger webDebugger;
  private final VMOptions options;
  
  @CompilationFinal private Assumption mateActivated;
  @CompilationFinal private Assumption mateDeactivated;
}
