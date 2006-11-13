package edu.mit.csail.pag.amock.trace;

import java.io.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.*;

import utilMDE.BCELUtil;

public class Premain {

  public static String traceFileName = "trace.xml";
  public static boolean verbose = false;
  public static boolean debug = false;
  public static boolean no_store_event = false;
  public static boolean no_load_event = false;
  public static boolean no_write_event = false;
  public static boolean no_read_event = false;
  public static boolean no_static_write_event = false;
  public static final File debug_dir = new File ("debug");
  public static final File debug_bin_dir = new File (debug_dir, "bin");
  public static final File debug_orig_dir = new File (debug_dir, "orig");
  public static final String compare_sets_file = null;

  public static void premain (String agentArgs, Instrumentation inst) {

    if (agentArgs != null) {
      String[] args = agentArgs.split ("(  *)|(, *)");
      String error_msg = parse_args (args);
      if (error_msg != null) {
        usage (error_msg);
        System.exit (1);
      }
    }
    if (verbose)
      System.out.format ("In trace premain, agentargs ='%s', " +
                         "Instrumentation = '%s'\n", agentArgs, inst);

    if (debug) {
      debug_bin_dir.mkdirs();
      debug_orig_dir.mkdirs();
    }

    // Setup the shutdown hook
    Thread shutdown_thread = new ShutdownThread();
    java.lang.Runtime.getRuntime().addShutdownHook (shutdown_thread);

    Transform statement = new Transform();
    inst.addTransformer (statement);

    // Initialize the trace file
    Runtime.setTraceFile(open (traceFileName));
  }

  static public class Transform implements ClassFileTransformer {

    public byte[] transform (ClassLoader loader, String className,
                             Class<?> classBeingRedefined,
                             ProtectionDomain protectionDomain,
                             byte[] classfileBuffer) {

      // Don't instrument JDK classes (but allow instrumentation of the java
      // compiler)
      if ((className.startsWith ("java/") || className.startsWith ("com/")
           || className.startsWith ("sun/") || className.startsWith("javax/"))
          && !className.startsWith ("com/sun/tools/javac"))
        return (null);

      // Don't instrument utilMDE classes
      if (className.startsWith ("utilMDE"))
        return null;

      // Don't instrument our own classes
      if ((className.startsWith ("edu/mit/csail/pag/amock/trace")))
        return (null);

      if (verbose)
        System.out.format ("In Transform: class = %s\n", className);

      try {
        // Parse the bytes of the classfile, die on any errors
        ClassParser parser = new ClassParser
          (new ByteArrayInputStream (classfileBuffer), className);
        JavaClass c = parser.parse();


        if (debug) {
          c.dump (new File (debug_orig_dir, c.getClassName() + ".class"));
        }

        // Transform the file
        Trace.TracingOptions options = new Trace.TracingOptions(no_read_event, no_write_event, no_load_event, no_store_event, no_static_write_event);
        Trace trace = new Trace(c, loader, options, Runtime.class);
        JavaClass njc = trace.instrument();
        if (njc == null) {
          if (verbose)
            System.out.printf ("Didn't instrument %s%n", c.getClassName());
          return (null);
        } else {
          if (debug) {
            System.out.printf ("Dumping to %s%n", debug_bin_dir);
            njc.dump (new File (debug_bin_dir, njc.getClassName() + ".class"));
            BCELUtil.dump (njc, debug_bin_dir);
          }
          return (njc.getBytes());
        }
      } catch (Throwable e) {
        System.out.printf ("Unexpected Error: %n");
        e.printStackTrace();
        throw new RuntimeException ("Unexpected error: " + e);
      }
    }
  }

  static String parse_args (String[] args) {

    for (int ii = 0; ii < args.length; ii++) {

      String arg = args[ii];
      if (arg.equals ("--verbose")) {
        verbose = true;
      } else if (arg.equals ("--debug")) {
        debug = true;
      } else if (arg.equals("--no-read-event")) {
        no_read_event = true;
      } else if (arg.equals("--no-write-event")) {
        no_write_event = true;
      } else if (arg.equals("--no-load-event")) {
        no_load_event = true;
      } else if (arg.equals("--no-store-event")) {
        no_store_event = true;
      } else if (arg.startsWith ("--tracefile=")){
        traceFileName = arg.substring("--tracefile=".length());
      } else {
        return ("Unexpected argument " + arg);
      }
    }
    return (null);
  }

  public static void usage (String msg) {

    System.out.println (msg);
    System.out.println ("trace <options>");
    System.out.println ("Options:");
    System.out.println ("  --tracefile=<filename>");
    System.out.println ("  --debug");
    System.out.println ("  --verbose");
    System.out.println ("  --no-read-event  Do not trace field read events");
    System.out.println ("  --no-write-event Do not trace field write events");
    System.out.println ("  --no-load-event  Do not trace array load events");
    System.out.println ("  --no-store-event Do not trace array store events");
  }

  /**
   * Shutdown thread that writes out the comparability results
   */
  public static class ShutdownThread extends Thread {

    @Override
      public void run() {
      Runtime.stop();
      System.out.printf ("Shut down.\n");
      java.lang.Runtime.getRuntime().halt(0);
    }
  }

  public static PrintStream open (String filename) {
    try {
      return new PrintStream (filename);
    } catch (Exception e) {
      throw new Error ("Can't open " + filename, e);
    }
  }
}
