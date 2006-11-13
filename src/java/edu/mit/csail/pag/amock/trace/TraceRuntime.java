package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;
import java.util.*;

import jpaul.Misc.*;

public class TraceRuntime {

  private static Tracer traceHandler= new Tracer(new Function<Object, Integer>(){
      @Override public Integer f(Object arg) {
        return id(arg);
      }
    }); 

  /**
   * Map from all objects seen by trace to an integer id.  Used both
   * to provide a consistent ID with a more reasonable number and also
   * to ensure that IDs are not reused
   */
  private static final WeakIdentityHashMap<Object,Integer> id_map
    = new WeakIdentityHashMap<Object,Integer>(new Action<Integer>() {
        public void action(Integer id) {
          removed(id);
        }
      });

  /** Stored id of garbage collected objects **/
  static Set<Integer> removed = new LinkedHashSet<Integer>(1);

  private static void removed(Integer id) {
    synchronized (removed) {
      removed.add(id);
    }
  }

  /** Next valid id for an object **/
  private static int obj_id = 0;

  /** Next valid id for a call **/
  private static int next_call_id = 0;

  public static void setTraceFile(PrintStream stream) {
    traceHandler.setTraceFile(stream);
  }

  public static void stop() {
    traceHandler.stop();
  }

  /**
   * Returns the integer id used to identify obj.  If this is the first
   * testtime obj has been seen, assigns it a new id
   */
  private static int id (Object obj) {
    if (id_map.containsKey (obj))
      return id_map.get (obj);

    id_map.put (obj, ++obj_id);
    return (obj_id);
  }

  /** Returns the next call id **/
  public static int get_call_id() {
    return (next_call_id++);
  }

  /** Resets the state to the original setting **/
  public static void reset() {
    traceHandler.reset();
    id_map.clear();
    next_call_id= 0;
    obj_id= 0;
  }

  /**
   * Called before a method is called.  Returns the current indent so that
   * the indent will be correct on exit even in the presence of exceptions
   */
  public static int enter (int call_id, Object receiver, Object[] args,
                           String method_signature) {
    traceHandler.enter(call_id, receiver, args, method_signature);
    return 0;  // don't care about indent
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
   * @param call_id The id of this call.  Matches up the exit with its
   *                corresponding enter
   **/
  public static void trace (Object ret_val, Object receiver, Object[] args,
                            String signature, int some_indent, int call_id) {
    traceHandler.trace(ret_val, receiver, args, signature, call_id);
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
    traceHandler.putfield(obj, val, field_name);
  }
  
  /**
   * Output a record for a static field write.

   *  @param val - value that will be written to the field.  Primitives
   *               are wrapped in PrimitiveWrapper
   *  @param field_name - Fully qualified name of the field being written to
   */
  public static void putstatic (Object val, String field_name) {
    traceHandler.putstatic(val, field_name);
  }
  
  /**
   * Output a record for a field read.
   *
   *  @param val - value read.  Primitives are wrapped in PrimitiveWrapper
   *  @param obj - object containing the field
   *  @param field_name - Fully qualified name of the field read
   */
  public static void getfield (Object val, Object obj, String field_name) {
    traceHandler.getfield(val, obj, field_name);
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
    traceHandler.arraystore(arr, index, val);
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
    traceHandler.arrayload(val, index, arr);
  }
}
