package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;
import java.util.*;

import jpaul.Misc.Function;

import edu.mit.csail.pag.amock.trace.Runtime.*;
import edu.mit.csail.pag.amock.trace.Wrap.*;

/**
 * Runtime support for tracing.  Writes the output to the trace file.
 * The following records are created:
 * <pre>
 *   ENTER [receiver] [signature] [arg1] [arg2]... [id]
 *   EXIT  [receiver] [signature] [arg1] [arg2]... =[return-val] [id]
 *   WRITE [field-name] [object] [value]
 *   READ  [field-name] [object] [value]
 *   ARRAYSTORE [array] [index] [arrayvalue]
 *   ARRAYLOAD  [array] [index] [arrayvalue]
 *
 * where
 *
 *  [receiver]      - object that is the receiver of the call.  Objects
 *                    are entered in the form #[obj-id]:[type] where [obj-id]
 *                    is a unique identifer of the object and type is its
 *                    class name
 *
 *  [signature]     - full signature of the method including the type of each
 *                    parameter.  Params are separated by commas (no spaces)
 *
 *  [argN]          - Nth argument to the method.  References are formatted
 *                    in the same was as receivers.  Primitives are formatted
 *                    as [val]:[type] where [val] is the value of the primitive
 *                    and type is its java type (int, long, etc)
 *
 *  [id]            - Caller ID.  An integer that matches up ENTER and EXIT
 *                    lines.  Enclosed in braces
 *  [return-val]    - Return value from the function.  Formatted in the same
 *                    manner as arguments
 *  [field-name]    - Fully qualified field name
 *  [object]        - Object being written to/read from.  In the same format as receiver
 *  [value]         - Value written to/read from the field.  Same format as [argN].
 *  [array]         - Array being stored to/loaded from. In the same format as receiver
 *  [index]         - Index in the array index. An integer in braces. 
 *  [arrayvalue]    - Value written to/loaded from the array.  Same format as [value].
 */
class Tracer {
  private boolean stopped = false;
  private PrintStream traceFile;
  private final Function<Object, Integer> id;
    
  public Tracer(Function<Object, Integer> id){
    if (id == null) {
      throw new IllegalArgumentException();
    }
    
    this.id = id;
  }

  public void reset() {
    traceFile = null;
    stopped = false;
  }
    
  /**
   * Quote \, ", \n, \r, and ' ' characters in the target;
   * return a new string. Blanks are quoted so that arguments in the trace
   * file are still blank separated
   **/
  private static String quote(String orig) {
    StringBuilder sb = new StringBuilder();
    // The previous escape (or escaped) character was seen right before
    // this position.  Alternately:  from this character forward, the string
    // should be copied out verbatim (until the next escaped character).
    int post_esc = 0;
    int orig_len = orig.length();
    for (int i=0; i<orig_len; i++) {
      char c = orig.charAt(i);
      switch (c) {
      case '\"':
      case '\\':
        if (post_esc < i) {
          sb.append(orig.substring(post_esc, i));
        }
        sb.append('\\');
        post_esc = i;
        break;
      case ' ':
        if (post_esc < i) {
          sb.append(orig.substring(post_esc, i));
        }
        sb.append ("\\040");
        post_esc = i+1;
        break;
      case '\t':
        if (post_esc < i) {
          sb.append(orig.substring(post_esc, i));
        }
        sb.append ("\\011");
        post_esc = i+1;
        break;
      case ':':
        if (post_esc < i) {
          sb.append(orig.substring(post_esc, i));
        }
        sb.append ("\\072");
        post_esc = i+1;
        break;
      case '\n':                // not lineSep
        if (post_esc < i) {
          sb.append(orig.substring(post_esc, i));
        }
        sb.append("\\n");       // not lineSep
        post_esc = i+1;
        break;
      case '\r':
        if (post_esc < i) {
          sb.append(orig.substring(post_esc, i));
        }
        sb.append("\\r");
        post_esc = i+1;
        break;
      default:
        // Do nothing; i gets incremented.
      }
    }
    if (sb.length() == 0)
      return orig;
    sb.append(orig.substring(post_esc));
    return sb.toString();
  }
    
  /** Return the trace format for the specified object **/
  private String trace_str (Object val) {
    if (val instanceof PrimitiveWrapper) {
      PrimitiveWrapper wrapper = (PrimitiveWrapper) val;
      return  wrapper.toString() + ":" + wrapper.type();
    } else if (val == null) {
      return "null";
    } else if (val instanceof String) {
      return "\"" + quote ((String) val) + "\":" + "java.lang.String";
    } else { // reference type
      return "#" + id.f(val) + ":" + val.getClass().getName();
    }
  }
  
  private void writeEscaped(String str) {
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


  
  private void printObject (Object val) {
    if (val instanceof PrimitiveWrapper) {
      PrimitiveWrapper wrapper = (PrimitiveWrapper) val;
      traceFile.print("<primitive type=\"" + wrapper.type() + "\" value=\"");
      // These are numbers, so no quoting necessary.
      traceFile.print(wrapper.toString());
      traceFile.println("\"/>");
    } else if (val == null) {
      traceFile.println("<null/>");
    } else if (val instanceof String) {
      // XXX QUOTING
      traceFile.print("<string>");
      writeEscaped((String) val);
      traceFile.println("</string>");
    } else { // reference type
      int idNum = id.f(val);
      traceFile.print("<object class=\"");
      writeEscaped(val.getClass().getName());
      traceFile.println("\" id=\"" + idNum + "\"/>");
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
  public void arrayload (Object val, int index, Object arr) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.println("<action type='getarray' index=\"" + index + "\">");
      traceFile.print("  <receiver>");
      printObject(arr);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</action>");
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
  public void arraystore (Object arr, int index, Object val) {
    synchronized (traceFile) {
      traceFile.println("<action type='setarray' index=\"" + index + "\">");
      traceFile.print("  <receiver>");
      printObject(arr);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</action>");
    }        
  }
    
  /**
   * Output a record for a field read.
   *
   *  @param val - value read.  Primitives are wrapped in PrimitiveWrapper
   *  @param obj - object containing the field
   *  @param field_name - Fully qualified name of the field read
   */
  public void getfield (Object val, Object obj, String field_name) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.print("<action type='getfield' field=\"");
      writeEscaped(field_name);
      traceFile.println("\">");
      traceFile.print("  <receiver>");
      printObject(obj);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</action>");
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
  public void putfield (Object obj, Object val, String field_name) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.print("<action type='setfield' field=\"");
      writeEscaped(field_name);
      traceFile.println("\">");
      traceFile.print("  <receiver>");
      printObject(obj);
      traceFile.println("  </receiver>");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</action>");
    }        
  }
    
  /**
   * Output a record for a static field write.
   *
   *  @param val - value that will be written to the field.  Primitives
   *               are wrapped in PrimitiveWrapper
   *  @param field_name - Fully qualified name of the field being written to
   */
  public void putstatic (Object val, String field_name) {
    if (stopped) return;
    synchronized (traceFile) {
      traceFile.print("<action type='setstatic' field=\"");
      writeEscaped(field_name);
      traceFile.println("\">");
      traceFile.println("  <value>");
      printObject(val);
      traceFile.println("  </value>\n</action>");
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
  public void trace (Object ret_val, Object receiver, Object[] args,
                     String signature, int call_id) {
    if (stopped) return;

    synchronized (traceFile) {
      traceFile.print("<action type='exit' call=\"" + call_id +
                      "\" signature=\"");
      writeEscaped(signature);
      traceFile.println("\">");

      if (receiver != null) {
        // Instance invokation.
        traceFile.print("<receiver>");
        printObject(receiver);
        traceFile.println("</receiver>");
      } else {
        assert signature.startsWith ("static ");
        traceFile.println("<static/>");
      }

      traceFile.println("<args>");
      for (Object arg : args) {
        printObject(arg);
      }
      traceFile.println("</args>");

      if (ret_val instanceof VoidWrap) {
        traceFile.println("<void/>");
      } else {
        traceFile.println("<return>");
        printObject(ret_val);
        traceFile.println("</return>");
      }

      traceFile.println("</action>");
    }
  }
    
  /**
   * Called before a method is called.
   */
  public void enter (int call_id, Object receiver, Object[] args,
                     String method_signature) {
    if (stopped) return;

    synchronized (traceFile) {
      printGC();

      traceFile.print("<action type='enter' call=\"" + call_id +
                      "\" signature=\"");
      writeEscaped(method_signature);
      traceFile.println("\">");

      if (receiver != null) {
        // Instance invokation.
        traceFile.print("<receiver>");
        printObject(receiver);
        traceFile.println("</receiver>");
      } else if (method_signature.startsWith("static ")) {
        traceFile.println("<static/>");
      } else {
        traceFile.println("<constructor/>");
      }

      traceFile.println("<args>");
      
      // Print each argument
      for (Object arg : args) {
        printObject(arg);
      }

      traceFile.println("</args>\n</action>");
    }
  }
    
  private void printGC() {
    synchronized (traceFile) {
      synchronized (Runtime.removed) {
        for (Iterator<Integer> iter = Runtime.removed.iterator(); iter.hasNext();) {
          Integer i = iter.next();
          traceFile.println("<action type='gc' id=\"" + i + "\"/>");
          iter.remove();
        } 
      }
    }
  }

  public void setTraceFile(PrintStream stream) {
    traceFile = stream;
    traceFile.println("<trace>");
  }

  public void stop() {
    traceFile.println("</trace>");
    stopped = true;
    traceFile.close();
  }

}
