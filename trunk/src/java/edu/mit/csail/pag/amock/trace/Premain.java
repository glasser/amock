package edu.mit.csail.pag.amock.trace;

import java.io.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.*;

import org.objectweb.asm.*;

public class Premain {

  private static String traceFileName = "trace.xml";
  private static boolean verbose = false;
  private static boolean debug = false;
  private static final File debugTransformedDir;
  private static final File debugOriginalDir;
  static {
    File debugDir = new File("premain-debug");
    debugTransformedDir = new File(debugDir, "transformed");
    debugOriginalDir = new File(debugDir, "original");
  }

  private static final List<String> nonTransformedPrefixes
    = Arrays.asList(new String[] {
        "java/",
        "com/sun/",
        "javax/",
        "sun/",
        "edu/mit/csail/pag/amock/trace/",
        "com/thoughtworks/xstream/",
        "net/sf/cglib/",
        "org/objectweb/asm/"});

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
    Tracer.setTraceFile(new PrintStream(traceFileName));
  }

  static private class Transform implements ClassFileTransformer {
    /**
     * Transforms class files at load time; called automatically by
     * the Instrumentation.
     */
    public byte[] transform (ClassLoader loader, String className,
                             Class<?> classBeingRedefined,
                             ProtectionDomain protectionDomain,
                             byte[] classfileBuffer) {

      for (String p : nonTransformedPrefixes) {
        if (className.startsWith(p)) {
          return null;
        }
      }
      
      if (verbose) {
        System.out.format ("In Transform: class = %s\n", className);
      }

      ClassWriter cw = new ClassWriter(true);
      ClassVisitor transformer = new TraceTransformer(cw);
      ClassReader cr = new ClassReader(classfileBuffer);
      cr.accept(transformer, true);
      byte[] transformed = cw.toByteArray();

      String name = Utils.getObjectType(className).getClassName();
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
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        p.close();
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

  private static void usage (String msg) {
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
  private static class ShutdownThread extends Thread {
    @Override
      public void run() {
      Tracer.stop();
      Runtime.getRuntime().halt(0);
    }
  }
}
