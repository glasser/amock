package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;
import java.util.*;

import jpaul.Misc.Function;

import palulu.purity.online.ITraceHandler;
import palulu.trace.Runtime.*;
import palulu.trace.Wrap.*;

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
class Tracer implements ITraceHandler{
    private static final String[] indent_strings;
    private static final int max_indent = 200;

  private boolean stopped = false;

    static {
      indent_strings = new String[max_indent];
      for (int ii = 0; ii < max_indent; ii++) {
          char[] spaces= new char[ii];
          Arrays.fill(spaces, ' ');
          indent_strings[ii]= new String(spaces);
      }
    }
    
    private boolean parenthesize;
    private PrintStream trace_file;
    private final Function<Object, Integer> id;
    
    public Tracer(Function<Object, Integer> id){
        if (id == null) throw new IllegalArgumentException();
        this.id = id;
        this.parenthesize= false;
    }

    public void reset() {
        indent= 0;
        trace_file= null;
        parenthesize= false;
        stopped = false;
    }

    private void indent() {
        trace_file.print (indent_strings[indent]);
    }

    /** Current level of indentation **/
    private int indent = 0;
    
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
        synchronized (trace_file) {

            indent();
            trace_file.println ("ARRAYLOAD " + trace_str (arr) + " [" + index + "]"
                                + " " + trace_str (val));
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
        synchronized (trace_file) {

            indent();
            trace_file.println ("ARRAYSTORE " + trace_str (arr) + " [" + index + "]"
                                + " " + trace_str (val));
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
        synchronized (trace_file) {

          indent();
          trace_file.println ("READ " + field_name + " " + trace_str (obj)
                              + " " + trace_str (val));
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
        synchronized (trace_file) {

          indent();
          trace_file.println ("WRITE " + field_name + " " + trace_str (obj)
                              + " " + trace_str (val));
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
        synchronized (trace_file) {

          indent();
          trace_file.println ("SWRITE " + field_name
                              + " " + trace_str (val));
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
            String signature, int enter_indent, int call_id) {
            if (stopped) return;

        synchronized (trace_file) {
            indent = enter_indent;
            indent();

            if (receiver != null)
              trace_file.printf ("EXIT #%d:%s %s", id.f(receiver),
                                 receiver.getClass().getName(), signature);
            else {
              assert signature.startsWith ("static");
              trace_file.printf ("EXIT %s", signature);
            }

            for (Object arg : args) {
                trace_file.print(" ");
                trace_file.print (trace_str (arg));
            }

            if (!(ret_val instanceof VoidWrap)) {
              trace_file.print (" =" + trace_str (ret_val));
            }

            trace_file.println (" [" + call_id + "]");

            if (parenthesize) {
                trace_file.printf (")");
            }
          }
    }
    
    /**
     * Called before a method is called.  Returns the current indent so that
     * the indent will be correct on exit even in the presence of exceptions
     */
    public int enter (int call_id, Object receiver, Object[] args,
            String method_signature) {
            if (stopped) return 0;

        synchronized (trace_file) {
            printGC();
            indent();
            if (parenthesize) {
              trace_file.printf ("(");
            }
            if (receiver != null)
              trace_file.print ("ENTER #" + id.f(receiver) + ":" +
                            receiver.getClass().getName() + " " + method_signature);
            else if (method_signature.startsWith ("static "))
              trace_file.print ("ENTER " + method_signature);
            else // must be a constructor
              trace_file.print ("ENTER #0:uninit " + method_signature);

            // Print each argument
            for (Object arg : args) {
                trace_file.print(" ");
                trace_file.print (trace_str (arg));
            }

            // Print the call id (allows enter/exits to be matched up)
            trace_file.println (" [" + call_id + "]");
            indent += 2;
          }
          return (indent - 2);
    }
    
    private void printGC() {
        synchronized (trace_file) {
            synchronized (Runtime.removed) {
                for (Iterator<Integer> iter = Runtime.removed.iterator(); iter.hasNext();) {
                    Integer i = iter.next();
                    indent();
                    trace_file.println ("GC #" + i );
                    iter.remove();
                } 
            }
        }
    }

    public void setTraceFile(PrintStream stream) {
        trace_file= stream;
    }

    public void stop() {
      stopped = true;
        trace_file.close();
    }

    public void setParameterize(boolean b) {
        parenthesize= b;
    }

    public void setOutputFileName(String name) {
        //do nothing 
        //XXX this should not be needed
    }    
}
