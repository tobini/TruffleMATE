/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
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

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import som.interpreter.Invokable;
import som.interpreter.TruffleCompiler;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.MateClasses;
import som.vmobjects.SArray;
import som.vmobjects.SBasicObjectLayoutImpl;
import som.vmobjects.SBlock;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SObjectLayoutImpl;
import som.vmobjects.SInvokable.SMethod;
import som.vmobjects.SInvokable.SPrimitive;
import som.vmobjects.SObject;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class Universe extends ExecutionContext {

  public static void main(final String[] arguments) {
    Universe u = current();
    try {
      u.interpret(arguments);
      u.exit(0);
    } catch (IllegalStateException e) {
      errorExit(e.getMessage());
    }
  }

  public Object interpret(String[] arguments) {
    // Check for command line switches
    arguments = handleArguments(arguments);

    // Initialize the known universe
    return execute(arguments);
  }

  protected Universe() {
    this.truffleRuntime = Truffle.getRuntime();
    this.avoidExit    = false;
    this.alreadyInitialized = false;
    this.lastExitCode = 0;
    objectMemory = new ObjectMemory();
    vmReflectionEnabled = false;
  }
  
  public void initalize() {
    objectMemory.initializeSystem();
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
    current().exit(1);
  }

  @TruffleBoundary
  public String[] handleArguments(String[] arguments) {
    boolean gotClasspath = false;
    String[] remainingArgs = new String[arguments.length];
    int cnt = 0;

    for (int i = 0; i < arguments.length; i++) {
      if (arguments[i].equals("-cp")) {
        if (i + 1 >= arguments.length) {
          printUsageAndExit();
        }
        setupClassPath(arguments[i + 1]);
        ++i;
        gotClasspath = true;
      } else if (arguments[i].equals("-d")) {
        objectMemory.setASTPrinting();
      } else if (arguments[i].equals("-activateMate")) {
        this.activatedMate();
      } else if (arguments[i].equals("--mate")) {
        this.enableVMReflection();
      }else {
        remainingArgs[cnt++] = arguments[i];
      }
    }

    if (!gotClasspath) {
      // Get the default class path of the appropriate size
      objectMemory.setClassPath(setupDefaultClassPath(0));
    }

    // Copy the remaining elements from the original array into the new
    // array
    arguments = new String[cnt];
    System.arraycopy(remainingArgs, 0, arguments, 0, cnt);

    // check remaining args for class paths, and strip file extension
    for (int i = 0; i < arguments.length; i++) {
      String[] split = getPathClassExt(arguments[i]);
      if (!("".equals(split[0]))) { // there was a path
        objectMemory.addPath(split[0]);
      }
      arguments[i] = split[1];
    }

    return arguments;
  }

  @TruffleBoundary
  // take argument of the form "../foo/Test.som" and return
  // "../foo", "Test", "som"
  private String[] getPathClassExt(final String arg) {
    File file = new File(arg);

    String path = file.getParent();
    StringTokenizer tokenizer = new StringTokenizer(file.getName(), ".");

    if (tokenizer.countTokens() > 2) {
      errorPrintln("Class with . in its name?");
      exit(1);
    }

    String[] result = new String[3];
    result[0] = (path == null) ? "" : path;
    result[1] = tokenizer.nextToken();
    result[2] = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";

    return result;
  }

  @TruffleBoundary
  public void setupClassPath(final String cp) {
    // Create a new tokenizer to split up the string of directories
    StringTokenizer tokenizer = new StringTokenizer(cp, File.pathSeparator);

    // Get the default class path of the appropriate size
    String [] classPath = setupDefaultClassPath(tokenizer.countTokens());

    // Get the directories and put them into the class path array
    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
      classPath[i] = tokenizer.nextToken();
    }
    
    objectMemory.setClassPath(classPath);
  }

  @TruffleBoundary
  private String[] setupDefaultClassPath(final int directories) {
    // Get the default system class path
    String systemClassPath = System.getProperty("system.class.path");

    // Compute the number of defaults
    int defaults = (systemClassPath != null) ? 2 : 1;

    // Allocate an array with room for the directories and the defaults
    String[] result = new String[directories + defaults];

    // Insert the system class path into the defaults section
    if (systemClassPath != null) {
      result[directories] = systemClassPath;
    }

    // Insert the current directory into the defaults section
    result[directories + defaults - 1] = ".";

    // Return the class path
    return result;
  }

  private void printUsageAndExit() {
    // Print the usage
    println("Usage: som [-options] [args...]                          ");
    println("                                                         ");
    println("where options include:                                   ");
    println("    -cp <directories separated by " + File.pathSeparator + ">");
    println("                  set search path for application classes");
    println("    -d            enable disassembling");

    // Exit
    System.exit(0);
  }

  /**
   * Start interpretation by sending the selector to the given class. This is
   * mostly meant for testing currently.
   *
   * @param className
   * @param selector
   * @return
   */
  public Object interpret(final String className, final String selector) {
    if (!alreadyInitialized) {
      objectMemory.initializeSystem();
      alreadyInitialized = true;
    }

    DynamicObject clazz = objectMemory.loadClass(symbolFor(className), null);

    // Lookup the initialize invokable on the system class
    DynamicObject initialize = SClass.lookupInvokable(SObject.getSOMClass(clazz),
        symbolFor(selector));
    return SInvokable.invoke(initialize, MateClasses.STANDARD_ENVIRONMENT, ExecutionLevel.Base, clazz);
  }

  private Object execute(final String[] arguments) {
    if (!alreadyInitialized) {
      objectMemory.initializeSystem();
      alreadyInitialized = true;
    }
    
    // Start the shell if no filename is given
    if (arguments.length == 0) {
      Shell shell = new Shell(this);
      return shell.start();
    }

    // Lookup the initialize invokable on the system class
    DynamicObject initialize = SClass.lookupInvokable(
        systemClass, symbolFor("initialize:"));

    return SInvokable.invoke(initialize, MateClasses.STANDARD_ENVIRONMENT, ExecutionLevel.Base, objectMemory.getSystemObject(), SArray.create(arguments));
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

  public boolean isObjectSystemInitialized() {
    return objectSystemInitialized;
  }

  public static Universe getCurrent(){
    return current;
  }

  public static void setCurrent(final Universe universe){
    current = universe;
  }

  public DynamicObjectFactory getInstancesFactory(){
    return SObject.SOBJECT_FACTORY;
  }
  
  public static Universe current() {
    if (current == null) {
      current = new Universe();
    }
    return current;
  }
  
  public void activatedMate(){};
  
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
    return SObjectLayoutImpl.INSTANCE.createSObjectShape(dummyObjectForInitialization).newInstance();
  }
  
  public DynamicObjectFactory createObjectShapeFactoryForClass(final DynamicObject clazz) {
    return SObject.createObjectShapeFactoryForClass(clazz);
  }
  
  public DynamicObject create(DynamicObject clazz){
    return SObject.create(clazz);
  }
  
  public void enableVMReflection(){
    vmReflectionEnabled = true;
  }
  
  public boolean vmReflectionEnabled(){
    return vmReflectionEnabled;
  }
  
  public DynamicObject getTrueObject()   { return objectMemory.getTrueObject(); }
  public DynamicObject getFalseObject()  { return objectMemory.getFalseObject(); }
  public DynamicObject getSystemObject() { return objectMemory.getSystemObject(); }
  
  private final TruffleRuntime                  truffleRuntime;
  // TODO: this is not how it is supposed to be... it is just a hack to cope
  //       with the use of system.exit in SOM to enable testing
  @CompilationFinal private boolean             avoidExit;
  private int                                   lastExitCode;

  // Latest instance
  // WARNING: this is problematic with multiple interpreters in the same VM...
  @CompilationFinal private static Universe current;
  @CompilationFinal protected boolean alreadyInitialized;
  @CompilationFinal protected boolean vmReflectionEnabled;
  @CompilationFinal public boolean objectSystemInitialized = false;
  private final ObjectMemory objectMemory;
}
