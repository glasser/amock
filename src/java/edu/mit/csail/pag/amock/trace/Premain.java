package edu.mit.csail.pag.amock.trace;

import java.io.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.*;

import org.objectweb.asm.*;

import edu.mit.csail.pag.amock.util.*;

public class Premain {

  private static String traceFileName = "trace.xml";
  private static String hierarchyFileName = "hierarchy.xml";
  private static boolean verbose = false;
  private static boolean debug = false;
  private static final File debugTransformedDir;
  private static final File debugOriginalDir;
  static {
    File debugDir = new File("premain-debug");
    debugTransformedDir = new File(debugDir, "transformed");
    debugOriginalDir = new File(debugDir, "original");
  }

  public static final List<String> nonTransformedPrefixes
    = Arrays.asList(new String[] {
        "java/",
        "com/sun/",
        "javax/",
        "sun/",
        "apple/",
        "edu/mit/csail/pag/amock/",
        "edu/mit/csail/pag/smock/",
        "com/thoughtworks/xstream/",
        "net/sf/cglib/",
        "org/xml/",
        "junit/",
        "org/jmock/",
        "org/hamcrest/",
        "org/objectweb/asm/"});

  public static final List<String> transformAnywayPrefixes
    = Arrays.asList(new String[] {
        "edu/mit/csail/pag/amock/subjects/",
        "edu/mit/csail/pag/smock/tests/"});

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

    PrintStream hierarchyStream = new PrintStream(hierarchyFileName);
    Serializer<HierarchyEntry> hierarchySer
      = Serializer.getSerializer(hierarchyStream);
    
    // Setup the shutdown hook
    Thread shutdownThread = new ShutdownThread(hierarchyStream, hierarchySer);
    Runtime.getRuntime().addShutdownHook(shutdownThread);

    Transform statement = new Transform(hierarchySer);
    inst.addTransformer(statement);

    // Initialize the trace file
    Tracer.setTraceFile(new PrintStream(traceFileName));
  }

  public static boolean shouldTransform(ClassName name) {
    String className = name.slashed();

    for (String p : transformAnywayPrefixes) {
      if (className.startsWith(p)) {
        return true;
      }
    }

    for (String p : nonTransformedPrefixes) {
      if (className.startsWith(p)) {
        return false;
      }
    }

    return true;
  }

  static private class Transform implements ClassFileTransformer {
    private final Serializer<HierarchyEntry> hierarchyDump;
    
    private Transform(Serializer<HierarchyEntry> hierarchyDump) {
      this.hierarchyDump = hierarchyDump;
    }
    
    /**
     * Transforms class files at load time; called automatically by
     * the Instrumentation.
     */
    public byte[] transform (ClassLoader loader, String className,
                             Class<?> classBeingRedefined,
                             ProtectionDomain protectionDomain,
                             byte[] classfileBuffer) {
      if (!shouldTransform(ClassName.fromSlashed(className))) {
        return null;
      }
      
      if (verbose) {
        System.out.format ("In Transform: class = %s\n", className);
      }

      ClassWriter cw = new ClassWriter(true);
      ClassVisitor transformer = new TraceTransformer(cw, hierarchyDump);
      ClassReader cr = new ClassReader(classfileBuffer);
      cr.accept(transformer, true);
      byte[] transformed = cw.toByteArray();

      ClassName cn = ClassName.fromSlashed(className);
      dumpToDir(debugOriginalDir, cn, classfileBuffer);
      dumpToDir(debugTransformedDir, cn, transformed);
      
      return transformed;
    }
  }

  private static void dumpToDir(File dir, ClassName cn, byte[] buf) {
    if (debug) {
      File fakeFile = new File(dir, cn.slashed());
      File parent = fakeFile.getParentFile();
      String baseName = fakeFile.getName();
      parent.mkdirs();
      File realFile = new File(parent, baseName + ".class");
      
      try {
        FileOutputStream p = null;
        try {
          p = new FileOutputStream(realFile);
          p.write(buf);
        } finally {
          if (p != null) {
            p.close();
          }
        }
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
      } else if (arg.startsWith ("--hierarchyfile=")){
        hierarchyFileName = arg.substring("--hierarchyfile=".length());
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
    System.out.println ("  --hierarchyfile=<filename> (filename of class hierarchy dump)");
    System.out.println ("  --debug (write classes to disk");
    System.out.println ("  --verbose (show extra messages)");
  }

  /**
   * Shutdown thread that allows the tracer to write out any final
   * information.
   */
  private static class ShutdownThread extends Thread {
    private final PrintStream hierarchyStream;
    private final Serializer<HierarchyEntry> hierarchySer;
    private ShutdownThread(PrintStream hierarchyStream,
                           Serializer<HierarchyEntry> hierarchySer) {
      this.hierarchyStream = hierarchyStream;
      this.hierarchySer = hierarchySer;
    }
    
    @Override
      public void run() {
      Tracer.stop();
      hierarchySer.close();
      hierarchyStream.close();
    }
  }
}
