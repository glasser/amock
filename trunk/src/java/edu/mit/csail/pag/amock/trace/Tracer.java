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
   * Output a record for an array load.
   *
   *  @param val - value that was read from the array.  Primitives
   *               are wrapped in PrimitiveWrapper
   *  @param index - the store index
   *  @param arr   - the array object
   */
  public static void arrayload (Object val, int index, Object arr) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.println("<getarray index=\"" + index + "\">");
      traceFile.print("  <receiver>");
      printObject(arr);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</getarray>");
    }
  }
    
  /**
   * Output a record for an array store.
   *
   *  @param arr   - the array object
   *  @param index - the store index
   *  @param val - value that will be written to the array.  Primitives
   *               are wrapped in PrimitiveWrapper
   */
  public static void arraystore (Object arr, int index, Object val) {
    synchronized (traceFile) {
      traceFile.println("<setarray index=\"" + index + "\">");
      traceFile.print("  <receiver>");
      printObject(arr);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</setarray>");
    }        
  }
    
  /**
   * Output a record for a field read.
   *
   *  @param val - value read.  Primitives are wrapped in PrimitiveWrapper
   *  @param obj - object containing the field
   *  @param field_name - Fully qualified name of the field read
   */
  public static void getfield (Object val, Object obj, String field_name) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.print("<getfield field=\"");
      writeEscaped(field_name);
      traceFile.println("\">");
      traceFile.print("  <receiver>");
      printObject(obj);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</getfield>");
    }
  }
    
  /**
   * Output a record for a field write.
   *
   *  @param obj - object containing the field
   *  @param val - value that will be written to the field.  Primitives
   *               are wrapped in PrimitiveWrapper
   *  @param field_name - Fully qualified name of the field being written to
   */
  public static void putfield (Object obj, Object val, String field_name) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.print("<setfield field=\"");
      writeEscaped(field_name);
      traceFile.println("\">");
      traceFile.print("  <receiver>");
      printObject(obj);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</setfield>");
    }        
  }
    
  /**
   * Output a record for a static field write.
   *
   *  @param val - value that will be written to the field.  Primitives
   *               are wrapped in PrimitiveWrapper
   *  @param field_name - Fully qualified name of the field being written to
   */
  public static void putstatic (Object val, String field_name) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.print("<setstatic field=\"");
      writeEscaped(field_name);
      traceFile.println("\">");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</setstatic>");
    }
  }

  /**
   * Output an exit line for this call.
   *
   * @param ret_val return value for the method.  VoidWrap is passed is
   *                the method is void.  Primitives are wrapped
   * @param receiver 'this' for the method call.  Null if method is static
   * @param args array of the arguments for the method.  Primitives are
   *             wrapped
   * @param signature String signature of method including its declared class
   * @param enter_indent The number of spaces indented when the method was
   *                     entered.  The indent is returned to this value
   * @param call_id The id of this call.  Matchs up the exit with its
   *                corresponding enter
   **/
  public static void tracePostCall(Object ret_val, Object receiver, Object[] args,
                                   String owner, String name, String desc, int call_id) {
    if (stopped) return;

    synchronized (traceFile) {
      traceFile.print("<postCall call=\"" + call_id +
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

      if (ret_val == VOID_RETURN_VALUE) {
        traceFile.println("<void/>");
      } else {
        traceFile.println("<return>");
        printObject(ret_val);
        traceFile.println("</return>");
      }

      traceFile.println("</postCall>");
    }
  }
    
  /**
   * Called before a method is called.
   */
  public static void tracePreCall(Object receiver, Object[] args,
                                  String owner, String name, String desc, int call_id) {
    if (stopped) return;

    synchronized (traceFile) {
      printGC();

      traceFile.print("<preCall call=\"" + call_id +
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
