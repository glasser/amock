package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;
import java.util.*;

import jpaul.Misc.Action;

/**
 * Runtime support for tracing.  Writes an XML stream.
 */
public class Tracer {
  private static boolean stopped = false;
  private static PrintStream traceFile;

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
  public static int getCallId() {
    return nextCallId++;
  }

  private static void writeEscaped(String str) {
    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      if (ch == '"') {
        traceFile.print("&quot;");
      } else if (ch == '&') {
        traceFile.print("&amp;");
      } else if (ch == '<') {
        traceFile.print("&lt;");
      } else if (ch == '>') {
        traceFile.print("&gt;");
      } else {
        traceFile.print(ch);
      }
    }
  }
  
  private static void printObject (Object val) {
    String boxedValue = boxedPrimitiveValue(val);
    
    if (boxedValue != null) {
      // These are numbers, so no quoting necessary.
      traceFile.print("<primitive type=\"" + val.getClass().getSimpleName()
                      + "\" value=\"" + boxedValue + "\"/>");
    } else if (val == null) {
      traceFile.println("<null/>");
    } else if (val instanceof String) {
      // XXX QUOTING
      traceFile.print("<string>");
      writeEscaped((String) val);
      traceFile.println("</string>");
    } else { // reference type
      int idNum = getId(val);
      traceFile.print("<object class=\"");
      writeEscaped(val.getClass().getName());
      traceFile.println("\" id=\"" + idNum + "\"/>");
    }
  }

  private static String boxedPrimitiveValue(Object val) {
    if (val instanceof Byte ||
        val instanceof Double ||
        val instanceof Float ||
        val instanceof Integer ||
        val instanceof Long ||
        val instanceof Short ||
        val instanceof Boolean) {
      return val.toString();
    } else if (val instanceof Character) {
      // XXX sign expansion?
      int i = ((Character)val).charValue();
      return String.valueOf(i);
    } else {
      return null;
    }
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
      traceFile.print("<postCall call=\"" + callId +
                      "\" owner=\"");
      writeEscaped(owner);
      traceFile.print("\" name=\"");
      writeEscaped(name);
      traceFile.print("\" descriptor=\"");
      writeEscaped(desc);
      traceFile.println("\">");

      if (receiver != null) {
        // Instance invokation.
        traceFile.print("<receiver>");
        printObject(receiver);
        traceFile.println("</receiver>");
      } else {
        // Not doing static right now.
//         assert signature.startsWith ("static ");
//         traceFile.println("<static/>");
      }

      traceFile.println("<args>");
      for (Object arg : args) {
        printObject(arg);
      }
      traceFile.println("</args>");

      if (retVal == VOID_RETURN_VALUE) {
        traceFile.println("<void/>");
      } else {
        traceFile.println("<return>");
        printObject(retVal);
        traceFile.println("</return>");
      }

      traceFile.println("</postCall>");
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

      traceFile.print("<preCall call=\"" + callId +
                      "\" owner=\"");
      writeEscaped(owner);
      traceFile.print("\" name=\"");
      writeEscaped(name);
      traceFile.print("\" descriptor=\"");
      writeEscaped(desc);
      traceFile.println("\">");

      if (receiver != CONSTRUCTOR_RECEIVER) {
        // Instance invokation.
        traceFile.print("<receiver>");
        printObject(receiver);
        traceFile.println("</receiver>");
        // XXX not making static right now
//       } else if (method_signature.startsWith("static ")) {
//         traceFile.println("<static/>");
      } else {
        traceFile.println("<constructor/>");
      }

      traceFile.println("<args>");
      
      // Print each argument
      for (Object arg : args) {
        printObject(arg);
      }

      traceFile.println("</args>\n</preCall>");
    }
  }
    
  private static void printGC() {
    synchronized (traceFile) {
      synchronized (removed) {
        for (Iterator<Integer> iter = removed.iterator(); iter.hasNext();) {
          Integer i = iter.next();
          traceFile.println("<gc id=\"" + i + "\"/>");
          iter.remove();
        } 
      }
    }
  }

  public static void setTraceFile(PrintStream stream) {
    traceFile = stream;
    traceFile.println("<trace>");
  }

  public static void stop() {
    traceFile.println("</trace>");
    stopped = true;
    traceFile.close();
  }

  public static final Object VOID_RETURN_VALUE = new Object();
  public static final Object CONSTRUCTOR_RECEIVER = new Object();
}
