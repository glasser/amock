package edu.mit.csail.pag.amock.trace;

import java.io.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.regex.Pattern;

import org.objectweb.asm.*;

public class Premain {

  public static String traceFileName = "trace.xml";
  public static boolean verbose = false;
  public static boolean debug = false;
  public static final File debugTransformedDir;
  public static final File debugOriginalDir;
  static {
    File debugDir = new File("premain-debug");
    debugTransformedDir = new File(debugDir, "transformed");
    debugOriginalDir = new File(debugDir, "original");
  }

  /**
   * Called when Java is invoked with -javaagent pointing to a jar
   * with this class as premain agent.
   */
  public static void premain (String agentArgs, Instrumentation inst)
    throws FileNotFoundException {
    
    if (agentArgs != null) {
      String[] args = agentArgs.split ("(  *)|(, *)");
      String error = parseArgs (args);
      if (error != null) {
        usage(error);
        System.exit(1);
      }
    }
    
    if (verbose) {
      System.out.format ("In premain, agentargs ='%s', " +
                         "Instrumentation = '%s'\n", agentArgs, inst);
    }

    if (debug) {
      debugTransformedDir.mkdirs();
      debugOriginalDir.mkdirs();
    }

    // Setup the shutdown hook
    Thread shutdownThread = new ShutdownThread();
    Runtime.getRuntime().addShutdownHook(shutdownThread);

    Transform statement = new Transform();
    inst.addTransformer(statement);

    // Initialize the trace file
    TraceRuntime.setTraceFile(new PrintStream(traceFileName));
  }

  static public class Transform implements ClassFileTransformer {
    /**
     * Transforms class files at load time; called automatically by
     * the Instrumentation.
     */
    public byte[] transform (ClassLoader loader, String className,
                             Class<?> classBeingRedefined,
                             ProtectionDomain protectionDomain,
                             byte[] classfileBuffer) {

      // Don't instrument JDK classes (but allow instrumentation of the java
      // compiler)
      if ((className.startsWith ("java/") || className.startsWith ("com/")
           || className.startsWith ("sun/") || className.startsWith("javax/"))
          && !className.startsWith ("com/sun/tools/javac")) {
        return null;
      }

      // Don't instrument utilMDE classes - XXX kill when we stop using utilMDE
      if (className.startsWith ("utilMDE")) {
        return null;
      }

      // Don't instrument our own classes
      if ((className.startsWith ("edu/mit/csail/pag/amock/trace"))) {
        return null;
      }

      if (verbose) {
        System.out.format ("In Transform: class = %s\n", className);
      }

      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      ClassVisitor transformer = new TraceTransformer(cw);
      ClassReader cr = new ClassReader(classfileBuffer);
      cr.accept(transformer, 0);
      byte[] transformed = cw.toByteArray();

      String name = Type.getObjectType(className).getClassName();
      dumpToDir(debugOriginalDir, name, classfileBuffer);
      dumpToDir(debugTransformedDir, name, transformed);
      
      return transformed;
    }
  }

  private static void dumpToDir(File dir, String className, byte[] buf) {
    if (debug) {
      try {
        FileOutputStream p = new FileOutputStream(new File(dir,
                                                           className + ".class"));
        p.write(buf);
        p.close();
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static String parseArgs (String[] args) {
    for (int ii = 0; ii < args.length; ii++) {
      String arg = args[ii];
      if (arg.equals ("--verbose")) {
        verbose = true;
      } else if (arg.equals ("--debug")) {
        debug = true;
      } else if (arg.startsWith ("--tracefile=")){
        traceFileName = arg.substring("--tracefile=".length());
      } else {
        return ("Unexpected argument " + arg);
      }
    }
    // No error.
    return null;
  }

  public static void usage (String msg) {
    System.out.println ("Error: " + msg);
    System.out.println ("trace <options>");
    System.out.println ("Options:");
    System.out.println ("  --tracefile=<filename> (filename of output trace)");
    System.out.println ("  --debug (write classes to disk");
    System.out.println ("  --verbose (show extra messages)");
  }

  /**
   * Shutdown thread that allows the tracer to write out any final
   * information.
   */
  public static class ShutdownThread extends Thread {
    @Override
      public void run() {
      TraceRuntime.stop();
      Runtime.getRuntime().halt(0);
    }
  }
}
