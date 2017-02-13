package som;

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;

import som.vm.Universe;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class VMOptions {
  public final String[] args;
  public final boolean showUsage;

  @CompilationFinal public boolean debuggerEnabled;
  @CompilationFinal public boolean webDebuggerEnabled;
  @CompilationFinal public boolean profilingEnabled;
  @CompilationFinal public boolean dynamicMetricsEnabled;
  @CompilationFinal public boolean highlightingEnabled;
  @CompilationFinal public String [] classPath;
  @CompilationFinal public boolean printAST;
  @CompilationFinal public boolean vmReflectionEnabled;
  @CompilationFinal public boolean vmReflectionActivated;

  public VMOptions(final String[] args) {
    vmReflectionEnabled = false;
    printAST = false;
    classPath = setupDefaultClassPath(0);
    this.args = processVmArguments(args);
    showUsage = args.length == 0;
    if (!VmSettings.INSTRUMENTATION &&
        (debuggerEnabled || webDebuggerEnabled || profilingEnabled ||
        dynamicMetricsEnabled || highlightingEnabled)) {
      throw new IllegalStateException(
          "Instrumentation is not enabled, but one of the tools is used. " +
          "Please set -D" + VmSettings.INSTRUMENTATION_PROP + "=true");
    }
  }

  private String[] processVmArguments(final String[] arguments) {
    int currentArg = 0;
    boolean parsedArgument = true;

    while (parsedArgument) {
      if (currentArg >= arguments.length) {
        return null;
      } else {
        if (arguments[currentArg].equals("--debug")) {
          debuggerEnabled = true;
          currentArg += 1;
        } else if (arguments[currentArg].equals("--web-debug")) {
          webDebuggerEnabled = true;
          currentArg += 1;
        } else if (arguments[currentArg].equals("--profile")) {
          profilingEnabled = true;
          currentArg += 1;
        } else if (arguments[currentArg].equals("--dynamic-metrics")) {
          dynamicMetricsEnabled = true;
          currentArg += 1;
        } else if (arguments[currentArg].equals("--highlight")) {
          highlightingEnabled = true;
          currentArg += 1;
        } else if (arguments[currentArg].equals("-cp")) {
          if (currentArg + 1 >= arguments.length) {
            printUsageAndExit();
          }
          setupClassPath(arguments[currentArg + 1]);
          currentArg += 2;
        } else if (arguments[currentArg].equals("-d")) {
          printAST = true;
        } else if (arguments[currentArg].equals("-activateMate")) {
          vmReflectionActivated = true;
          currentArg += 1;
        } else if (arguments[currentArg].equals("--mate")) {
          vmReflectionEnabled = true;
          currentArg += 1;
        } else {
          parsedArgument = false;
        }
      }
    }

    // store remaining arguments
    if (currentArg < arguments.length) {
      return Arrays.copyOfRange(arguments, currentArg, arguments.length);
      /*
      // check remaining args for class paths, and strip file extension
      for (int i = 0; i < arguments.length; i++) {
        String[] split = getPathClassExt(arguments[i]);
        if (!("".equals(split[0]))) { // there was a path
          objectMemory.addPath(split[0]);
        }
        arguments[i] = split[1];
      }

      return arguments;*/
    } else {
      return null;
    }
  }

  public static void printUsageAndExit() {
    Universe.println("VM arguments, need to come before any application arguments:");
    Universe.println("");
    Universe.println("  --debug                Run in Truffle Debugger/REPL");
    Universe.println("  --web-debug            Start web debugger");
    Universe.println("");
    Universe.println("  --profile              Enable the TruffleProfiler");
    Universe.println("  --dynamic-metrics      Enable the DynamicMetrics tool");
    Universe.println("  --highlight            Enable the Highlight tool"); // TODO: this should take a parameter at some point, but for that we need to be able to access config options from tools
    Universe.println("alternative options include:                                   ");
    Universe.println("    -cp <directories separated by " + File.pathSeparator + ">");
    Universe.println("                  set search path for application classes");
    Universe.println("    -d            enable disassembling");
    Universe.getCurrent().exit(1);
  }

  @TruffleBoundary
  public void setupClassPath(final String cp) {
    // Create a new tokenizer to split up the string of directories
    StringTokenizer tokenizer = new StringTokenizer(cp, File.pathSeparator);

    // Get the default class path of the appropriate size
    classPath = setupDefaultClassPath(tokenizer.countTokens());

    // Get the directories and put them into the class path array
    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
      classPath[i] = tokenizer.nextToken();
    }
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
}
