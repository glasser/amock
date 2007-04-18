package edu.mit.csail.pag.amock.trace;

import java.util.*;

// Note: ASM 2 is required.
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/**
 * A ClassVisitor which adds tracing calls to the class it is
 * visiting.
 */
public class TraceTransformer extends ClassAdapter {
  private String className;
  
  public TraceTransformer(ClassVisitor cv) {
    super(cv);
  }

  public void visit(int version,
                    int access,
                    String className,
                    String signature,
                    String superName,
                    String[] interfaces) {
    this.className = className;
    super.visit(version, access, className, signature, superName, interfaces);
  }


  /**
   * Implement the ClassVisitor visitMethod method; makes a
   * MethodVisitor that is a TraceMethodTransformer instead of the
   * default implementation.
   */
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions) {
    MethodVisitor mv = cv.visitMethod(access, name, desc,
                                      signature, exceptions);

    // If for some reason the next ClassVisitor down the chain thinks
    // this method isn't worth visiting, don't visit it.
    if (mv == null) {
      return null;
    }

    return new TraceMethodTransformer(mv, access, className, name, desc);
  }

  /**
   * A MethodVisitor which adds tracing calls to the method it is
   * visiting.
   */
  public static class TraceMethodTransformer extends GeneratorAdapter {
    private final String thisClassName;
    private final String thisName;
    private final String thisDesc;
    
    public TraceMethodTransformer(MethodVisitor mv,
                                  int access,
                                  String thisClassName,
                                  String thisName,
                                  String thisDesc) {
      super(mv, access, thisName, thisDesc);
      this.thisClassName = thisClassName;
      this.thisName = thisName;
      this.thisDesc = thisDesc;
    }

    // The Type of java.lang.Object; cached as it is used several
    // times.
    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    // The Type of the class which trace calls get sent to; cached as
    // it is used several times.
    private static final Type TRACE_RUNTIME_TYPE =
      Type.getType(Tracer.class);

    /**
     * Insert a call to the method on the runtime class described by
     * javaDesc; the arguments must already be on the stack.
     */
    private void insertRuntimeCall(String javaDesc) {
      // TODO: Cache Method lookups.
      Method m = Method.getMethod(javaDesc);
      invokeStatic(TRACE_RUNTIME_TYPE, m);
    }

    /**
     * Instrument method entry.  (This allows us to see methods that
     * are called from uninstrumented code.)
     */
    public void visitCode() {
      push(thisClassName);
      push(thisName);
      push(thisDesc);
      insertRuntimeCall("void methodEntry(String, String, String)");
    }

    /**
     * Instrument method calls.
     */
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc) {
      if (opcode == Opcodes.INVOKEVIRTUAL ||
          opcode == Opcodes.INVOKESPECIAL ||
          opcode == Opcodes.INVOKEINTERFACE) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        Type returnType = Type.getReturnType(desc);
        Type receiverType = Utils.getObjectType(owner);

        // STACK: ... this args

        // Note: in the case of a constructor call, the "this" might
        // be an uninitialized object; the JVM won't allow us to store
        // them into local variables.  In "STACK" diagrams, "this"
        // will indicate a possibly-uninitialized receiver, "THIS" a
        // definitely-initialized one, and "this!" either a receiver
        // or the CONSTRUCTOR_RECEIVER object which replaces it.

        // Allocate locals and save argument values into them.
        // TODO: optimize by reusing locals across different
        //       instrumentations of the same method?
        int[] argLocals = new int[argTypes.length];

        // Iterate backwards, since the last argument is on top of the
        // stack.
        for (int i = argTypes.length - 1; i >= 0; i--) {
          int argumentLocal = newLocal(argTypes[i]);
          argLocals[i] = argumentLocal;
          storeLocal(argumentLocal);
        }

        // STACK: ... this

        // Save the receiver (or something representing it) into a
        // local (but keep it on the stack).
        int receiverLocal = newLocal(receiverType);
        if (name.equals("<init>")) {
          getStatic(TRACE_RUNTIME_TYPE,
                    "CONSTRUCTOR_RECEIVER",
                    OBJECT_TYPE);
        } else {
          duplicate(receiverType);
        }

        // STACK: ... this this!
        storeLocal(receiverLocal);

        // Get a call ID from the Tracer class.
        int callIdLocal = newLocal(Type.INT_TYPE);
        insertRuntimeCall("int getNextCallId()");
        storeLocal(callIdLocal);

        // STACK: ... this

        // Set up the arguments for tracePreCall.
        loadLocal(receiverLocal);
        pushArrayOfLocals(argLocals);
        push(owner);
        push(name);
        push(desc);
        loadLocal(callIdLocal);

        // STACK: ... this this! [args] owner name desc callid

        insertRuntimeCall("void tracePreCall(Object, Object[], String, "
                          + "String, String, int)");

        // STACK: ... THIS

        // Note that if the receiver wasn't initialized, it is now.
        // If we hadn't saved it before, now is our chance.
        if (name.equals("<init>")) {
          duplicate(receiverType);
          storeLocal(receiverLocal);
        }
        
        // Now push the arguments back onto the stack.
        for (int local : argLocals) {
          loadLocal(local);
        }

        // STACK: ... THIS args
        
        // Actually make the method call.
        mv.visitMethodInsn(opcode, owner, name, desc);

        // Put something representing the return value on top of the
        // stack: either the VOID_RETURN_VALUE object from the runtime
        // class, or the return value itself (boxed if it was a
        // primitive).
        if (returnType.getSort() == Type.VOID) {
          getStatic(TRACE_RUNTIME_TYPE,
                    "VOID_RETURN_VALUE",
                    OBJECT_TYPE);
        } else {
          duplicate(returnType);
          box(returnType);
        }

        // Set up the rest of the arguments for tracePostCall.
        loadLocal(receiverLocal);
        pushArrayOfLocals(argLocals);
        push(owner);
        push(name);
        push(desc);
        loadLocal(callIdLocal);
        
        // STACK: ... retval-boxed THIS [args] owner name desc callid

        insertRuntimeCall("void tracePostCall(Object, Object, Object[], "
                          + "String, String, String, int)");
      } else {
        // XXX: deal with static invokes.

        // Do the actual method call itself.
        mv.visitMethodInsn(opcode, owner, name, desc);
      }
    }

    /**
     * Assembles either DUP or DUP2, depending on the size of the
     * type.
     */
    private void duplicate(Type t) {
      if (t.getSize() == 2) {
        dup2();
      } else {
        dup();
      }
    }

    /**
     * Pushes an array containing the values of the locals in
     * someLocals, which must have been created with newLocal.
     */
    private void pushArrayOfLocals(int[] someLocals) {
      // First, make an empty array of the right size:
      assert someLocals.length >= Byte.MIN_VALUE &&
        someLocals.length <= Byte.MAX_VALUE;
      push(someLocals.length);
      newArray(OBJECT_TYPE);
      
      for (int i = 0; i < someLocals.length; i++) {
        // Duplicate the array reference.
        dup();
        push(i);
        loadLocal(someLocals[i]);
        // This is an Object array, so box the value if needed.
        box(getLocalType(someLocals[i]));
        arrayStore(OBJECT_TYPE);
      }
    }
  }
}