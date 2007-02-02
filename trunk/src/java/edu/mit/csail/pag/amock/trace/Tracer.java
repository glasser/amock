package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;
import java.util.*;

/**
 * Runtime support for tracing.  Writes an XML stream.  The format is
 * not the one described in tools/trace-schema.rnc; rather, it comes
 * from XStream.
 */
public class Tracer {
  private static boolean stopped = false;
  private static PrintStream traceFile;
  private static Serializer serializer;

  /** Next valid id for an object **/
  private static int nextObjId = 0;

  /** Next valid id for a call **/
  private static int nextCallId = 0;
  
  /**
   * Map from all objects seen by trace to an integer id.  Used both
   * to provide a consistent ID with a more reasonable number and also
   * to ensure that IDs are not reused
   */
  private static final WeakIdentityHashMap<Object,Integer> idMap
    = new WeakIdentityHashMap<Object,Integer>(new Action<Integer>() {
        public void action(Integer id) {
          removed(id);
        }
      });

  /** Stored id of garbage collected objects **/
  private static Set<Integer> removed = new LinkedHashSet<Integer>();

  /** GC callback from idMap */
  private static void removed(Integer id) {
    synchronized (removed) {
      removed.add(id);
    }
  }

  /**
   * Returns an integer id used to identify obj.  If this is the
   * first time obj has been seen, this assigns it a new id.
   */
  private static int getId(Object obj) {
    if (idMap.containsKey(obj)) {
      return idMap.get(obj);
    }

    idMap.put(obj, nextObjId);
    return nextObjId++;
  }

  /** Returns the next call ID. */
  public static int getNextCallId() {
    return nextCallId++;
  }

  private static TraceObject getTraceObject(Object val) {
    if (val == null || isBoxedPrimitiveOrString(val)) {
      // This encoding means that we can't trace code that uses
      // TraceObjects directly, but we're already not instrumenting
      // the trace package.
      return new Primitive(val);
    } else if (val instanceof TraceObject) {
      // For VoidReturnValue, etc.
      return (TraceObject) val;
    } else {
      String className = val.getClass().getName();
      int id = getId(val);
      return new Instance(className, id);
    }
  }

  private static TraceObject[] getTraceObjects(Object[] vals) {
    TraceObject[] tos = new TraceObject[vals.length];

    for (int i = 0; i < vals.length; i++) {
      tos[i] = getTraceObject(vals[i]);
    }

    return tos;
  }

  private static boolean isBoxedPrimitiveOrString(Object val) {
    return
      val instanceof Byte ||
      val instanceof Character ||
      val instanceof Double ||
      val instanceof Float ||
      val instanceof Integer ||
      val instanceof Long ||
      val instanceof Short ||
      val instanceof Boolean ||
      val instanceof String;
  }
  
  /**
   * Output a postCall element for this call.
   *
   * @param retVal return value for the method.  VOID_RETURN_VALUE
   *               passed if the method returns void.  Primitives are
   *               boxed.
   *                
   * @param receiver 'this' for the method call.  For constructors,
   *                 this is the constructed value.
   *
   * @param args array of the arguments for the method.  Primitives
   *             are boxed.
   *             
   * @param owner Class name of defining method, in internal form.
   *
   * @param name Method name
   *
   * @param desc Descriptor for method
   * 
   * @param callId An identifier for this call.  Matches up the
   *               postCall with its corresponding preCall.
   **/
  public static void tracePostCall(Object retVal, Object receiver,
                                   Object[] args, String owner, String name,
                                   String desc, int callId) {
    if (stopped) return;

    synchronized (traceFile) {
      TraceEvent e =
        new PostCall(callId,
                     new TraceMethod(owner, name, desc),
                     getTraceObject(receiver),
                     getTraceObjects(args),
                     getTraceObject(retVal));
      serializer.write(e);
    }
  }
    
  /**
   * Output a preCall element for this call.
   *
   * @param receiver 'this' for the method call.  For constructors,
   *                 this is CONSTRUCTOR_RECEIVER, because the object
   *                 is not initialized yet.
   *
   * @param args array of the arguments for the method.  Primitives
   *             are boxed.
   *             
   * @param owner Class name of defining method, in internal form.
   *
   * @param name Method name
   *
   * @param desc Descriptor for method
   * 
   * @param callId An identifier for this call.  Matches up the
   *               postCall with its corresponding preCall.
   */
  public static void tracePreCall(Object receiver, Object[] args, String owner,
                                  String name, String desc, int callId) {
    if (stopped) return;

    synchronized (traceFile) {
      printGC();

      TraceEvent e =
        new PreCall(callId,
                    new TraceMethod(owner, name, desc),
                    getTraceObject(receiver),
                    getTraceObjects(args));

      serializer.write(e);

      // TODO: check if rec is CONSTRUCTOR_RECEIVER

    }
  }

  // This currently doesn't work, since it is not using XStream.
  // On the other hand, we're not actually using it.
  private static void printGC() {
    synchronized (traceFile) {
      synchronized (removed) {
        for (Iterator<Integer> iter = removed.iterator(); iter.hasNext();) {
          Integer i = iter.next();
          // Printing out some sort of GC element could go here.
          iter.remove();
        } 
      }
    }
  }

  public static void setTraceFile(PrintStream stream) {
    traceFile = stream;
    serializer = new Serializer(stream);
  }

  public static void stop() {
    stopped = true;
    serializer.close();
    traceFile.close();
  }

  public static final Object VOID_RETURN_VALUE = new VoidReturnValue();
  public static final Object CONSTRUCTOR_RECEIVER = new ConstructorReceiver();
}
