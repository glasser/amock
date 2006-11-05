package edu.mit.csail.pag.amock.trace;

import java.io.*;
import java.util.*;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.VerificationResult;

import utilMDE.*;
import daikon.dcomp.StackVer;

/**
 * Instruments a class to traces each method call
 */
public class Trace {

  private final String runtimeClassName;

/** ClassGen for transformed class **/
  private final ClassGen gen;

  /** Constant pool for transformed class **/
  private final ConstantPoolGen pool;

  /** Instruction factory for transformed class **/
  private final InstructionFactory ifact;

  // Debug loggers
  private final SimpleLog debug_instrument = new SimpleLog (false);
  private final SimpleLog debug_instrument_inst = new SimpleLog (false);

  /** Object used to identify functions without return values **/
  private final ObjectType void_type
    = new ObjectType (Wrap.class.getName() + "$VoidWrap");
  
  /** Calls to ignore **/
  private static final String[] ignore_calls_array = new String[] {
    "java.lang.Object.<init>()",
  };

  private static final Set<String> ignore_calls = new LinkedHashSet<String>(Arrays.asList(ignore_calls_array));  
  
  /** Indicates whether instrumented methods are run through a verifier. */
  private static final boolean VERIFY = false;

  /** Type designator for an Array of Objects **/
  private final Type object_arr = new ArrayType (Type.OBJECT, 1);

  /** Options describe which events are traced.*/
  private final TracingOptions options;

private ClassLoader loader;

  public static void ignoreCalls(Set<String> userCallsToIgnore) {
      for(String ignoreCall:userCallsToIgnore) {
          ignore_calls.add(ignoreCall); 
      }   
  }
  /**
   * Initialize with the original class.
   * Runtime class is the class to which all calls will be delegated. 
   * The calls must be static and Java does not allow static methods in interfaces so this is the best we have.
   * The runtime class must implement the following static methods:
   *     public static void arrayload (Object val, int index, Object arr)
   *     public static void arraystore (Object arr, int index, Object val)
   *     public static int get_call_id()
   *     public static int enter (int call_id, Object receiver, Object[] args, String method_signature)
   *     public static void getfield (Object val, Object obj, String field_name)
   *     public static void putfield (Object obj, Object val, String field_name)
   *     public static void trace (Object ret_val, Object receiver, Object[] args, String signature, int enter_indent, int call_id)
   */
  public Trace (JavaClass orig_class, ClassLoader loader, TracingOptions opt, Class<?> runtimeClass) {
    gen = new ClassGen (orig_class);
    pool = gen.getConstantPool();
    ifact = new InstructionFactory (gen);
    options = opt;
    runtimeClassName= runtimeClass.getName();
    this.loader = loader;
  }

/**
   * Instruments the original class to trace method calls and returns
   * the new definition.
   */
  public JavaClass instrument() {
    //System.out.printf("Instrumenting class %s loader:%s%n", gen.getClassName(), loader);
    debug_instrument.log ("Instrumenting class %s loader:%s%n", gen.getClassName(), loader);
    debug_instrument.indent();

    // Process each method
    for (Method m : gen.getMethods()) {
      process(m);
    }

    return (gen.getJavaClass().copy());
  }

  private void process(Method m) {
    try {
        MethodGen mg = new MethodGen (m, gen.getClassName(), pool);
        boolean has_code = (mg.getInstructionList() != null) ;
        debug_instrument.log ("  Processing method %s%n", m);
        debug_instrument.indent();

        if (has_code) {

          // If this is a non-static inner class constructor, move its super
          // call to the front of the method
          if (is_inner_class(gen) && BCELUtil.is_constructor(mg)) {
          //  System.out.printf ("inner class %s %n", gen.getClassName());
            fix_inner_class_constructors(mg);
          }

          instrument_method (mg);
          for (Attribute a : mg.getCodeAttributes()) {
            if (is_local_variable_type_table (a)) {
              mg.removeCodeAttribute (a);
            }
          }
          if (BCELUtil.is_clinit (mg))
            trace_clinit (mg);
          mg.setMaxLocals();
          mg.setMaxStack();
          gen.replaceMethod (m, mg.getMethod());
          
          if (VERIFY)
              verify(mg);
        }

        debug_instrument.exdent();
        
      } catch (Throwable t) {
        throw new Error ("Unexpected error processing " + gen.getClassName()
                         + "." + m.getName(), t);
      }
  }

  private void verify(MethodGen mg) {
      StackVer stackver = new StackVer ();
      VerificationResult vr = stackver.do_stack_ver (mg);
      if (vr != VerificationResult.VR_OK)
         System.err.printf ("Warning: StackVer failed for %s: %s%n", mg, vr);    
  }

/**
   * Instrument the specified method to trace its method calls
   */
  private void instrument_method (MethodGen mg) {

    // Loop through each instruction, making substitutions
    InstructionList il = mg.getInstructionList();
    for (InstructionHandle ih = il.getStart(); ih != null; ) {

      if (debug_instrument_inst.enabled()) {
        debug_instrument_inst.log ("instrumenting instruction %s%n", ih);
      }
      InstructionList new_il = null;

      // Remember the next instruction to process
      InstructionHandle next_ih = ih.getNext();

      // Get the translation for this instruction (if any)
      new_il = xform_inst (mg, ih);
      if (debug_instrument_inst.enabled())
        debug_instrument_inst.log ("  new inst: %s%n", new_il);

      // If this instruction was modified, replace it with the new
      // instruction list. If this instruction was the target of any
      // jumps or line numbers , replace them with the first
      // instruction in the new list
      replace_instructions (il, ih, new_il);

      ih = next_ih;
    }
  }

  /**
   * Replace instruction ih in list il with the instructions in new_il.  If
   * new_il is null, do nothing
   */
  private static void replace_instructions (InstructionList il,
                                InstructionHandle ih, InstructionList new_il) {

    if (new_il == null)
      return;

    // If there is only one new instruction, just replace it in the handle
    if (new_il.getLength() == 1) {
      ih.setInstruction (new_il.getEnd().getInstruction());
      return;
    }

    // Get the start and end instruction of the new instructions
    InstructionHandle new_end = new_il.getEnd();
    InstructionHandle new_start = il.insert (ih, new_il);

    // Move all of the branches from the old instruction to the new start
    il.redirectBranches (ih, new_start);

    // Move other targets to the new instuctions.
    if (ih.hasTargeters()) {
      for (InstructionTargeter it : ih.getTargeters()) {
        if (it instanceof LineNumberGen) {
          it.updateTarget (ih, new_start);
        } else if (it instanceof LocalVariableGen) {
          it.updateTarget (ih, new_end);
        } else if (it instanceof CodeExceptionGen) {
          CodeExceptionGen exc = (CodeExceptionGen)it;
          if (exc.getStartPC() == ih)
            exc.updateTarget (ih, new_start);
          else if (exc.getEndPC() == ih)
            exc.updateTarget(ih, new_end);
          else if (exc.getHandlerPC() == ih)
            exc.setHandlerPC (new_start);
          else
            System.out.printf ("Malformed CodeException: %s%n", exc);
        } else {
          System.out.printf ("unexpected target %s%n", it);
        }
      }
    }

    // Remove the old handle.  There should be no targeters left to it.
    try {
      il.delete (ih);
    } catch (Exception e) {
      throw new Error ("Can't delete instruction", e);
    }
  }

  /**
   * Transforms instructions to trace method calls.  Returns a list
   * of instructions that replaces the specified instruction.  Returns
   * null if the instruction should not be replaced.
   *
   *    @param mg Method being instrumented
   *    @param ih Handle of Instruction to translate
   */
  private InstructionList xform_inst (MethodGen mg, InstructionHandle ih) {

    Instruction inst = ih.getInstruction();

    switch (inst.getOpcode()) {

    case Constants.INVOKESTATIC:
    case Constants.INVOKEINTERFACE:
    case Constants.INVOKEVIRTUAL:
    case Constants.INVOKESPECIAL: {
      InstructionList il = trace_invokeinstruction(mg, (InvokeInstruction)inst);
      return (il);
    }

    case Constants.PUTFIELD: {
        if (options.no_write_event)
            return (null);
        InstructionList il = trace_putfield(mg, (PUTFIELD) inst);
        return (il);
    }
    
    case Constants.PUTSTATIC: {
        if(options.no_static_write_event)
            return (null);
        InstructionList il = trace_putstatic(mg, (PUTSTATIC)inst);
        return (il);
    }

    case Constants.GETFIELD:{
        if (options.no_read_event)
            return (null);
        InstructionList il = trace_getfield(mg, (GETFIELD) inst);
        return (il);        
    }

    case Constants.AASTORE:
    case Constants.BASTORE:
    case Constants.CASTORE:
    case Constants.DASTORE:
    case Constants.FASTORE:
    case Constants.IASTORE:
    case Constants.LASTORE:
    case Constants.SASTORE: {
        if (options.no_store_event)
            return (null);
        InstructionList il = trace_arraystore(mg, (ArrayInstruction)inst);
        return (il);
    }

    case Constants.AALOAD:
    case Constants.BALOAD:
    case Constants.CALOAD:
    case Constants.DALOAD:
    case Constants.FALOAD:
    case Constants.IALOAD:
    case Constants.LALOAD:
    case Constants.SALOAD: {
        if (options.no_load_event)
            return (null);
        InstructionList il = trace_arrayload(mg, (ArrayInstruction)inst);
        return (il);
    }

    default:
      return (null);
    }
  }

  /**
   * Write out a record for array loads.
   */
  private InstructionList trace_arrayload(MethodGen mg, ArrayInstruction inst) {
      InstructionList il = new InstructionList();

      Type element_type= inst.getType(pool);
      ArrayType array_type = new ArrayType(element_type, 1);

      // pop the value, index, array reference into locals
      List<LocalVariableGen> locals
        = pop_into_locals (mg, new Type[] {array_type, Type.INT}, il);
      LocalVariableGen ref_lv   = locals.get(0);
      LocalVariableGen index_lv = locals.get(1);
      
      // put them back (for the eventual call to arrayload)
      push_locals (locals, il);
    
      // Execute the original array instruction
      il.append (inst);

      //----------preparing the call to Runtime.arrayload -------------------

      //duplicate the retrieved value, wrapping primitives
      il.append (InstructionFactory.createDup (element_type.getSize()));
      if (element_type instanceof BasicType) {
         LocalVariableGen loc = mg.addLocalVariable ("ret_tmp", element_type,
                                                      null, null);
         il.append (InstructionFactory.createStore  (element_type, loc.getIndex()));
         il.append (create_wrapper (element_type, loc.getIndex()));
      }

      //push the index on the stack
      il.append (InstructionFactory.createLoad (Type.INT, index_lv.getIndex()));
      
      // Push the array reference on the stack
      il.append(InstructionFactory.createLoad (array_type, ref_lv.getIndex()));

      // Call the arraystore(val, arr, index) runtime method
      il.append (ifact.createInvoke (runtimeClassName, "arrayload",
                   Type.VOID, new Type[] {Type.OBJECT, Type.INT, Type.OBJECT},
                   Constants.INVOKESTATIC));

      return (il);
  }

  /**
   * Write out a record for array stores.
   */
  private InstructionList trace_arraystore(MethodGen mg, ArrayInstruction inst) {

      InstructionList il = new InstructionList();
      Type element_type= inst.getType(pool);
      ArrayType array_type = new ArrayType(element_type, 1);

      // pop the value, index, array reference into locals
      List<LocalVariableGen> locals
        = pop_into_locals (mg, new Type[] {array_type, Type.INT, element_type}, il);
      LocalVariableGen ref_lv   = locals.get(0);
      LocalVariableGen index_lv = locals.get(1);
      LocalVariableGen val_lv   = locals.get(2);

      // put them back (for the eventual call to arraystore)
      push_locals (locals, il);

      // Push the array reference on the stack
      il.append(InstructionFactory.createLoad (array_type, ref_lv.getIndex()));

      //push the index on the stack
      il.append (InstructionFactory.createLoad (Type.INT, index_lv.getIndex()));

      // Push the value on the stack.  Primitives are wrapped
      if (element_type instanceof BasicType)
        il.append (create_wrapper (element_type, val_lv.getIndex()));
      else
        il.append (InstructionFactory.createLoad (Type.OBJECT, val_lv.getIndex()));

      // Call the arraystore(arr, index, val) runtime method
      il.append (ifact.createInvoke (runtimeClassName, "arraystore",
                   Type.VOID, new Type[] {Type.OBJECT, Type.INT, Type.OBJECT},
                   Constants.INVOKESTATIC));

      // Execute the original array instruction
      il.append (inst);

      return (il);
  }

  /**
   * Write out a record for method invocations.
   */
  private InstructionList trace_invokeinstruction(MethodGen mg, InvokeInstruction ii) {
      InstructionList il = new InstructionList();
      Type[] arg_types = ii.getArgumentTypes(pool);
      String signature = build_signature (ii);
      boolean is_static = (ii instanceof INVOKESTATIC);

      // Skip calls that are not of interest
      if (ignore_call (ii)) {
      //    System.out.println(".");
       // System.out.printf ("ignoring %s%n",
       //                    ii.toString(pool.getConstantPool()));
        return null;
      }

      // Pop each argument into a local (so we can get it later)
      List<LocalVariableGen> locals = pop_into_locals (mg, arg_types, il);

      // Pop the receiver into a local (leaving a copy on the stack)
      LocalVariableGen receiver_local = null;
      if (!is_static) {
        Type receiver_type = ii.getClassType (pool);
        receiver_type = fix_arr_obj_type (receiver_type);
        receiver_local = mg.addLocalVariable("this_tmp", receiver_type,
                                             null, null);
        il.append (InstructionFactory.createDup (receiver_type.getSize()));
        il.append (InstructionFactory.createStore (receiver_type,
                                                   receiver_local.getIndex()));
      }

      // Push each argument back on the stack
      push_locals (locals, il);

      // Get the ID for this call
      LocalVariableGen id = mg.addLocalVariable ("id", Type.INT, null, null);
      il.append (ifact.createInvoke (runtimeClassName, "get_call_id",
                             Type.INT, Type.NO_ARGS, Constants.INVOKESTATIC));
      il.append (InstructionFactory.createDup (Type.INT.getSize()));
      il.append (InstructionFactory.createStore (Type.INT, id.getIndex()));

      // Create the call to enter and store away the indent.
      LocalVariableGen indent
        = mg.addLocalVariable ("indent", Type.INT, null, null);
      if (ii.getMethodName(pool).equals ("<init>") || is_static)
        il.append (new ACONST_NULL());
      else
        il.append (InstructionFactory.createLoad (receiver_local.getType(),
                                     receiver_local.getIndex()));
      il.append (create_array_objects (locals));
      il.append (ifact.createConstant (signature));
      il.append (ifact.createInvoke (runtimeClassName, "enter",
        Type.INT, new Type[] {Type.INT, Type.OBJECT, object_arr, Type.STRING},
        Constants.INVOKESTATIC));
      il.append (InstructionFactory.createStore (Type.INT, indent.getIndex()));

      // Make the original call
      il.append (ii);

      // Create a call to trace
      il.append (create_trace_call (mg, signature, receiver_local, locals,
                                    ii.getReturnType(pool), indent, id));
      return (il);
}

  /**
   * Write out a record for field reads.
   */
  private InstructionList trace_getfield(MethodGen mg, GETFIELD inst) {

      InstructionList il = new InstructionList();

      ObjectType obj_type = (ObjectType) inst.getReferenceType(pool);

      // pop the field reference into local
      List<LocalVariableGen> locals= pop_into_locals (mg, new Type[] {obj_type}, il);
      LocalVariableGen ref_lv = locals.get(0);

      // put it back on stack (for the call to getfield)
      push_locals (locals, il);

      // Execute the original getfield instruction
      il.append (inst);

      //----------preparing the call to Runtime.getfield -------------------
      
      //duplicate the retrieved value, wrapping primitives
      Type field_type = inst.getFieldType(pool);
      il.append (InstructionFactory.createDup (field_type.getSize()));
      if (field_type instanceof BasicType) {
         LocalVariableGen loc = mg.addLocalVariable ("ret_tmp", field_type,
                                                      null, null);
         il.append (InstructionFactory.createStore  (field_type, loc.getIndex()));
         il.append (create_wrapper (field_type, loc.getIndex()));
      }

      // Push the field reference on the stack
      il.append(InstructionFactory.createLoad (ref_lv.getType(), ref_lv.getIndex()));

      // Push the full name of the field on the stack
      String field_name = obj_type.getClassName() + "." +inst.getFieldName(pool);
      il.append (ifact.createConstant (field_name));

      // Call the putfield(val, obj, field_name) runtime method
      il.append (ifact.createInvoke (runtimeClassName, "getfield",
                   Type.VOID, new Type[] {Type.OBJECT, Type.OBJECT, Type.STRING},
                   Constants.INVOKESTATIC));

      return (il);
    }

  /**
   * Write out a record for field writes.
   */
  private InstructionList trace_putfield (MethodGen mg, PUTFIELD inst) {

    InstructionList il = new InstructionList();
    Type field_type = inst.getFieldType (pool);
    ObjectType obj_type = (ObjectType) inst.getReferenceType(pool);

    // pop the value and the field reference into locals
    List<LocalVariableGen> locals
      = pop_into_locals (mg, new Type[] {obj_type, field_type}, il);
    LocalVariableGen ref_lv = locals.get(0);
    LocalVariableGen val_lv = locals.get(1);

    // put them back (for the eventual call to putfield)
    push_locals (locals, il);

    // Push the field reference on the stack
    il.append(InstructionFactory.createLoad (ref_lv.getType(), ref_lv.getIndex()));

    // Push the value on the stack.  Primitives are wrapped
    if (field_type instanceof BasicType)
      il.append (create_wrapper (field_type, val_lv.getIndex()));
    else
      il.append (InstructionFactory.createLoad (Type.OBJECT, val_lv.getIndex()));

    // Push the full name of the field on the stack
    String field_name = obj_type.getClassName() + "." +inst.getFieldName(pool);
    il.append (ifact.createConstant (field_name));

    // Call the putfield(obj, val, field_name) runtime method
    il.append (ifact.createInvoke (runtimeClassName, "putfield",
                 Type.VOID, new Type[] {Type.OBJECT, Type.OBJECT, Type.STRING},
                 Constants.INVOKESTATIC));

    // Execute the original putfield instruction
    il.append (inst);
    return (il);
  }
  
  /**
   * Write out a record for field writes.
   */
  private InstructionList trace_putstatic (MethodGen mg, PUTSTATIC inst) {

    InstructionList il = new InstructionList();
    Type field_type = inst.getFieldType (pool);
    ObjectType obj_type = (ObjectType) inst.getReferenceType(pool);

    // pop the value into local
    List<LocalVariableGen> locals
      = pop_into_locals (mg, new Type[] {field_type}, il);
    LocalVariableGen val_lv = locals.get(0);

    // put it back (for the eventual call to putstatic)
    push_locals (locals, il);

    // Push the value on the stack.  Primitives are wrapped
    if (field_type instanceof BasicType)
      il.append (create_wrapper (field_type, val_lv.getIndex()));
    else
      il.append (InstructionFactory.createLoad (Type.OBJECT, val_lv.getIndex()));

    // Push the full name of the field on the stack
    String field_name = obj_type.getClassName() + "." +inst.getFieldName(pool);
    il.append (ifact.createConstant (field_name));
    
    // Call the putstatic(val, field_name) runtime method
    il.append (ifact.createInvoke (runtimeClassName, "putstatic",
                 Type.VOID, new Type[] {Type.OBJECT, Type.STRING},
                 Constants.INVOKESTATIC));

    // Execute the original putfield instruction
    il.append (inst);
    return (il);
  }

  /**
   * Push each local in locals on the stack, adding the instructions to
   * il.
   */
  private void push_locals (List<LocalVariableGen> locals, InstructionList il) {

    for (LocalVariableGen lvg : locals) {
      il.append (InstructionFactory.createLoad (lvg.getType(), lvg.getIndex()));
    }
  }

  /**
   * Pops items off of the stack into new local variables.
   *
   * @param mg MethodGen to be changed.
   * @param types array of types to be popped from the stack.  The
   * last item in the array is on the top of the stack.
   * @param il the pop instructions will be added to il
   *
   * @return list of local variables that contain the items that
   * were previously on the stack.
   */
  private List<LocalVariableGen> pop_into_locals (MethodGen mg, Type[] types,
                                                InstructionList il) {

    List<LocalVariableGen> locals
      = new ArrayList<LocalVariableGen>(types.length);

    for (int ii = types.length-1; ii >= 0; ii--) {
      Type t = types[ii];
      LocalVariableGen local = mg.addLocalVariable ("tmp" + ii, t, null, null);
      locals.add (0, local);
      il.append (InstructionFactory.createStore (t, local.getIndex()));
    }

    return (locals);
  }

  /**
   * Pushes the return value, object, parameters, and method signature
   * on the stack and calls the trace method in Runtime.  The
   * parameters are passed as an array of objects.
   */
   private InstructionList create_trace_call (MethodGen mgen, String signature,
                                      LocalVariableGen receiver,
                                      List<LocalVariableGen> args,
                                      Type ret_type, LocalVariableGen indent,
                                      LocalVariableGen id) {

     InstructionList il = new InstructionList();

     // Duplicate the return value (if any), wrapping primitives
     // System.out.printf ("trace: return of %s = %s%n", signature, ret_type);
     if (ret_type == Type.VOID) {
       il.append (ifact.createGetStatic (Wrap.class.getName(), "void_obj",
                                         void_type));
     } else {
       il.append (InstructionFactory.createDup (ret_type.getSize()));
       if (ret_type instanceof BasicType) {
         LocalVariableGen loc = mgen.addLocalVariable ("ret_tmp", ret_type,
                                                       null, null);
         il.append (InstructionFactory.createStore  (ret_type, loc.getIndex()));
         il.append (create_wrapper (ret_type, loc.getIndex()));
       }
     }

     // Push the receiver
     if (receiver != null)
       il.append (InstructionFactory.createLoad (receiver.getType(),
                                                 receiver.getIndex()));
     else
       il.append (new ACONST_NULL());

     // Create an array containing each of the arguments as objects
     il.append (create_array_objects (args));

     // Push the method signature
     il.append (ifact.createConstant (signature));

     // Push the original indent
     il.append (InstructionFactory.createLoad (Type.INT, indent.getIndex()));

     // Push the call id
     il.append (InstructionFactory.createLoad (Type.INT, id.getIndex()));

     // Call the trace method
     Type[] method_args = new Type[] {Type.OBJECT, Type.OBJECT, object_arr,
                                      Type.STRING, Type.INT, Type.INT};
     il.append (ifact.createInvoke (runtimeClassName, "trace",
                             Type.VOID, method_args, Constants.INVOKESTATIC));


     return (il);
   }

  /**
   * Add entry and exit traces to class initializers.  These have to
   * be added to the initializer and not the caller since we don't
   * see the caller
   */
  private void trace_clinit (MethodGen mg) {
    assert BCELUtil.is_clinit (mg);


    InstructionList il = new InstructionList();

   // System.out.printf ("clinit for %s, il = %s%n", mg, il);

    // Get the ID for this call
    LocalVariableGen id = mg.addLocalVariable ("id", Type.INT, null, null);
    il.append (ifact.createInvoke (runtimeClassName, "get_call_id",
                           Type.INT, Type.NO_ARGS, Constants.INVOKESTATIC));
    il.append (InstructionFactory.createDup (Type.INT.getSize()));
    il.append (InstructionFactory.createStore (Type.INT, id.getIndex()));

    // Create a local variable for the indent level
    LocalVariableGen indent = mg.addLocalVariable ("indent", Type.INT,
                                                   null, null);

    // Null for the receiver (since clinit is static)
    il.append (new ACONST_NULL());

    // Push an empty array for the arguments (since we have none)
    il.append (create_array_objects (new ArrayList<LocalVariableGen>()));

    // Push the signature
    il.append (ifact.createConstant ("static " + mg.getClassName()
                                     + ".<clinit>()"));

    // Call enter
    il.append (ifact.createInvoke (runtimeClassName, "enter",
      Type.INT, new Type[] {Type.INT, Type.OBJECT, object_arr, Type.STRING},
      Constants.INVOKESTATIC));

    // Store away the indent
    il.append (InstructionFactory.createStore (Type.INT, indent.getIndex()));

    // Add the code to the start of the method
    add_to_start (mg, il);

    //
    // Now build the code for the end of the method
    //

    il = new InstructionList();

    // push the return value (which is void)
    il.append (ifact.createGetStatic (Wrap.class.getName(), "void_obj",
                                         void_type));
    // Push the receiver
    il.append (new ACONST_NULL());

    // Push an empty array for the arguments (since we have none)
    il.append (create_array_objects (new ArrayList<LocalVariableGen>()));

    // Push the signature
    il.append (ifact.createConstant ("static " + mg.getClassName()
                                     + ".<clinit>()"));

    // Push the original indent
    il.append (InstructionFactory.createLoad (Type.INT, indent.getIndex()));

    // Push the call id
    il.append (InstructionFactory.createLoad (Type.INT, id.getIndex()));

    // Call the trace method
    Type[] method_args = new Type[] {Type.OBJECT, Type.OBJECT, object_arr,
                                      Type.STRING, Type.INT, Type.INT};
    il.append (ifact.createInvoke (runtimeClassName, "trace",
                             Type.VOID, method_args, Constants.INVOKESTATIC));

    // Add to the end of the method
    InstructionList current_list = mg.getInstructionList();
    InstructionHandle old_end = current_list.getEnd();
    InstructionHandle new_end = current_list.insert (old_end, il);
    current_list.redirectBranches  (old_end, new_end);
  }

  /** Adds code in nl to start of method mg * */
  //workaround for Daikon bug http://pag.csail.mit.edu/bugzilla/show_bug.cgi?id=556
  public static void add_to_start(MethodGen mg, InstructionList nl) {

    // Add the code before the first instruction
    InstructionList il = mg.getInstructionList();
    InstructionHandle old_start = il.getStart();
    InstructionHandle new_start = il.insert(nl);

    // Move any LineNumbers and local variable that currently point to
    // the first instruction to include the new instructions. Other
    // targeters (branches, exceptions) should not include the new
    // code
    if (old_start.hasTargeters()){
       for (InstructionTargeter it : old_start.getTargeters()) {
            if ((it instanceof LineNumberGen) || (it instanceof LocalVariableGen))
               it.updateTarget(old_start, new_start);
       }
    }
    
    mg.setMaxStack();
    mg.setMaxLocals();
  }
  /**
   * Creates instructions that will build an array of objects that contain
   * each of the values stored in the local variables identified by args
   * Primitive types are wrapped in Runtime.[Type]Wrap classes
   */
  private InstructionList create_array_objects (List<LocalVariableGen> args) {

    InstructionList il = new InstructionList();

    // Create an array of objects with elements for each argument
     il.append (ifact.createConstant (args.size()));
     il.append (ifact.createNewArray (Type.OBJECT, (short) 1));

     // Put each argument into the array
     for (int ii = 0; ii < args.size(); ii++) {
       il.append (InstructionFactory.createDup (object_arr.getSize()));
       il.append (ifact.createConstant (ii));
       LocalVariableGen lvg = args.get (ii);
       Type at = lvg.getType();
       if (at instanceof BasicType) {
         il.append (create_wrapper (at, lvg.getIndex()));
       } else { // must be reference of some sort
         il.append (InstructionFactory.createLoad (Type.OBJECT, lvg.getIndex()));
       }
       il.append (InstructionFactory.createArrayStore (Type.OBJECT));
     }

     return (il);
  }

  /**
   * Returns whether or not this invoke should be ignored.
   */
  private boolean ignore_call (InvokeInstruction inst) {
    String full_name = build_signature(inst);
   return ignore_calls.contains(full_name);
  }

  /**
   * Returns the signature for the invoke instruction.  Adds 'static '
   * in front of the signature for static calls.
   */
  private String build_signature (InvokeInstruction inst) {

    String method_name = inst.getMethodName (pool);
    String classname = inst.getClassName (pool);

    String arguments = "";
    for (Type arg_type : inst.getArgumentTypes (pool)) {
      if (arguments.length() > 0)
        arguments += ",";
      arguments += arg_type.toString();
    }

    if (inst instanceof INVOKESTATIC)
      return "static " + classname + "." + method_name + "(" + arguments + ")";
    else
      return classname + "." + method_name + "(" + arguments + ")";
  }

  /**
   * Creates code to put the local var/param at the specified var_index
   * into a wrapper appropriate for prim_type.  prim_type should be one
   * of the basic types (eg, Type.INT, Type.FLOAT, etc).  The wrappers
   * are those defined in Runtime.
   *
   * The stack is left with a pointer to the newly created wrapper at the
   * top.
   */
  private InstructionList create_wrapper (Type prim_type, int var_index) {

    String wrapper = null;
    switch (prim_type.getType()) {
    case Constants.T_BOOLEAN: wrapper = "BooleanWrap"; break;
    case Constants.T_BYTE:    wrapper = "ByteWrap"; break;
    case Constants.T_CHAR:    wrapper = "CharWrap"; break;
    case Constants.T_DOUBLE:  wrapper = "DoubleWrap"; break;
    case Constants.T_FLOAT:   wrapper = "FloatWrap"; break;
    case Constants.T_INT:     wrapper = "IntWrap"; break;
    case Constants.T_LONG:    wrapper = "LongWrap"; break;
    case Constants.T_SHORT:   wrapper = "ShortWrap"; break;
    default:
      assert false : "unexpected type " + prim_type;
    }

    InstructionList il = new InstructionList();
    String classname = Wrap.class.getName() + "$" + wrapper;
    il.append (ifact.createNew (classname));
    il.append (InstructionFactory.createDup (Type.OBJECT.getSize()));
    il.append (InstructionFactory.createLoad (prim_type, var_index));
    il.append (ifact.createInvoke (classname, "<init>", Type.VOID,
                             new Type[] {prim_type}, Constants.INVOKESPECIAL));

    return (il);
  }

  private boolean is_local_variable_type_table (Attribute a) {
    return (get_attribute_name (a).equals ("LocalVariableTypeTable"));
  }

  /**
   * Returns the attribute name for the specified attribute
   */
  private String get_attribute_name (Attribute a) {

    int con_index = a.getNameIndex();
    Constant c = pool.getConstant (con_index);
    String att_name = ((ConstantUtf8) c).getBytes();
    return (att_name);
  }

  /**
   * Fix array types that somehow get put in object types (happens with
   * the type returned from invokeinstruction because they didn't consider
   * that there are virtual calls on arrays (clone, etc)
   */
  private Type fix_arr_obj_type (Type type) {

    if ((type instanceof ObjectType) && (type.toString().startsWith ("["))) {
      String classname = type.toString();
      int dimensions = 1;
      while (classname.charAt(dimensions) == '[')
        dimensions++;
      classname = classname.substring (dimensions);
      Type fix_type = null;
      if (classname.startsWith ("L"))
        fix_type = new ArrayType (classname.substring (1), dimensions);
      else
        fix_type = new ArrayType (basechar_to_type (classname.charAt (0)),
                                  dimensions);
      // System.out.printf ("translated %s to %s%n", type, fix_type);
      return (fix_type);
    }
    return (type);
  }

  /**
   * Returns the BCEL BasicType that that corresponds to the char basetype (eg,
   * I, D, Z, etc). See section 4.3.2 of of the JVM spec.
   */
  private static BasicType basechar_to_type(char basetype) {

    switch (basetype) {

    case 'B':
      return (Type.BYTE);
    case 'C':
      return (Type.CHAR);
    case 'D':
      return (Type.DOUBLE);
    case 'F':
      return (Type.FLOAT);
    case 'I':
      return (Type.INT);
    case 'J':
      return (Type.LONG);
    case 'S':
      return (Type.SHORT);
    case 'Z':
      return (Type.BOOLEAN);
    case 'V':
      return (Type.VOID);
    default:
      assert false : "unexpected basetype " + basetype;
      return (null);
    }
  }

  /**
   * Determines whether mg represents a non-static inner class (one with a
   * pointer to its enclosing class). This uses the name of the field that
   * references the enclosing class (this$0) as a key.
   */
  private boolean is_inner_class(ClassGen gen) {
    return (gen.containsField("this$0") != null);
  }

  /**
   * Inner classes initialize their synthetic fields (the reference to
   * their enclosing class and any final variables referenced within
   * the class) before calling the superconstructor. Since the object
   * is not valid before the super constructor is called, our accessor
   * calls will fail verify. We fix this by moving the super
   * constructor call to the beginning of the method.
   */
  private void fix_inner_class_constructors(MethodGen mg) {

    assert (BCELUtil.is_constructor(mg)) : mg.toString();
    assert (is_inner_class(gen)) : gen.toString();
    InstructionList il = mg.getInstructionList();

    // System.out.printf ("%s.%s flags = %X\n", gen.getClassName(), mg,
    // mg.getAccessFlags());

    // Only modify constructors that are synthetic or are for anonymous
    // classes (anonymous classes are named ...$1, ...$2, etc)
    // jhp 1/22/2006,  not sure why this check makes sense.  the problem
    // happens for named inner classes as well...
    //if (!is_syn && !classname.matches(".*\\$[0-9]$"))
    //  return;

    // Find the super class initializer
    String super_classname = gen.getSuperclassName();
    InstructionHandle super_call = null;
    INVOKESPECIAL is = null;
    int instruction_offset = 0;
    for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
      Instruction inst = ih.getInstruction();
      if (inst.getOpcode() == Constants.INVOKESPECIAL) {
        is = (INVOKESPECIAL) inst;
        // System.out.printf ("invokespecial: %s [%s.%s] %s (%s)\n", is,
        // is.getClassName(pool), is.getMethodName (pool),
        // super_classname, is.getType (pool));
        if (is.getMethodName(pool).equals("<init>")) {
          // System.out.printf ("matched at offset %d\n", instruction_offset);
          super_call = ih;
          break;
        }
      }
      instruction_offset++;
    }

    // Move the super call and its preceeding pushes to the front
    try {
      assert (super_call != null) : String.format("%s.%s [superclass %s]", gen
          .getClassName(), mg, super_classname);
      InstructionHandle arg = super_call.getPrev();
      Instruction super_inst = super_call.getInstruction();
      remove_line_number_targeters(super_call);
      il.delete(super_call);
      il.insert(super_inst);
      Type[] arg_types = is.getArgumentTypes(pool);
      for (int num_args = arg_types.length + 1; num_args > 0; num_args--) {
        InstructionHandle parg = arg.getPrev();
        Instruction push_inst = arg.getInstruction();
        remove_line_number_targeters(arg);
        il.delete(arg);
        il.insert(push_inst);
        arg = parg;
      }
    } catch (Exception e) {
      throw new Error("error moving super call in " + gen.getClassName() + "."
          + mg, e);
    }
  }

  private void remove_line_number_targeters(InstructionHandle ih) {

    // System.out.printf ("Moving instruction %s\n", ih);
    InstructionTargeter[] its = ih.getTargeters();
    if (its == null)
      return;
    for (InstructionTargeter it : its) {
      if ((it instanceof LineNumberGen) || (it instanceof LocalVariableGen))
        ih.removeTargeter(it);
      else
        System.out.printf("unexpected targeter %s to %s\n", it, ih);
    }
  }
  
  
  public static final class TracingOptions{
      public final boolean no_read_event;
      public final boolean no_write_event;
      public final boolean no_load_event;
      public final boolean no_store_event;
      public final boolean no_static_write_event;
      public TracingOptions(boolean no_read_event, boolean no_write_event, boolean no_load_event, boolean no_store_event, boolean no_static_write_event) {
        this.no_read_event = no_read_event;
        this.no_write_event = no_write_event;
        this.no_load_event = no_load_event;
        this.no_store_event = no_store_event;
        this.no_static_write_event = no_static_write_event;
    }
  }
}
