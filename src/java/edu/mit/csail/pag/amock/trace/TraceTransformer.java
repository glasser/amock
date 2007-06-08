package edu.mit.csail.pag.amock.trace;

import java.util.*;

// Note: ASM 2 is required.
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

import edu.mit.csail.pag.amock.util.Misc;

/**
 * A ClassVisitor which adds tracing calls to the class it is
 * visiting.
 */
public class TraceTransformer extends ClassAdapter {
  private String className;
  private String superName;
  private final Serializer<HierarchyEntry> hierarchyDump;
  
  public TraceTransformer(ClassVisitor cv,
                          Serializer<HierarchyEntry> hierarchyDump) {
    super(cv);
    this.hierarchyDump = hierarchyDump;
  }

  public void visit(int version,
                    int access,
                    String className,
                    String signature,
                    String superName,
                    String[] interfaces) {
    this.className = className;
    this.superName = superName;
    super.visit(version, access, className, signature, superName, interfaces);
    hierarchyDump.write(new HierarchyEntry(className, superName, interfaces));
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

    // Don't instrument static init.
    if (name.equals("<clinit>")) {
      return mv;
    }

    return new TraceMethodTransformer(mv, access, className, superName,
                                      name, desc);
  }

  /**
   * A MethodVisitor which adds tracing calls to the method it is
   * visiting.
   */
  public static class TraceMethodTransformer extends GeneratorAdapter {
    private final boolean isStatic;
    private final String thisClassName;
    private final String thisSuperName;
    private final String thisName;
    private final String thisDesc;

    // This local is used to store a call ID for the method itself
    // (not the method it calls).  This call ID is not the same as the
    // call ID of the pre/post call entries.
    private final int methodCallIdLocal = newLocal(Type.INT_TYPE);
    
    public TraceMethodTransformer(MethodVisitor mv,
                                  int access,
                                  String thisClassName,
                                  String thisSuperName,
                                  String thisName,
                                  String thisDesc) {
      super(mv, access, thisName, thisDesc);
      this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
      this.thisClassName = thisClassName;
      this.thisSuperName = thisSuperName;
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
      mv.visitCode();
      
      // Get a call ID for the method itself.
      insertRuntimeCall("int getNextCallId()");
      storeLocal(methodCallIdLocal);
      
      if (thisName.equals("<init>")) {
        getStatic(TRACE_RUNTIME_TYPE,
                  "CONSTRUCTOR_RECEIVER",
                  OBJECT_TYPE);
      } else if (isStatic) {
        push((String)null);
      } else {
        loadThis();
      }
      loadArgArray();
      push(thisClassName);
      push(thisName);
      push(thisDesc);
      loadLocal(methodCallIdLocal);
      insertRuntimeCall("void methodEntry(Object, Object[], String, String, String, int)");
    }

    /**
     * Instrument return instructions.
     */
    public void visitInsn(int opcode) {
      if (opcode == Opcodes.IRETURN ||
          opcode == Opcodes.LRETURN ||
          opcode == Opcodes.FRETURN ||
          opcode == Opcodes.DRETURN ||
          opcode == Opcodes.ARETURN ||
          opcode == Opcodes.RETURN) {
        push(thisClassName);
        push(thisName);
        push(thisDesc);
        loadLocal(methodCallIdLocal);
        insertRuntimeCall("void methodExit(String, String, String, int)");
      }

      // Do the actual instruction.
      mv.visitInsn(opcode);
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
        Type receiverType = Misc.getObjectType(owner);

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
        push(thisClassName);
        push(thisName);
        push(thisDesc);

        // STACK: ... this this! [args] owner name desc callid myOwner myName myDesc

        insertRuntimeCall("void tracePreCall(Object, Object[], String, "
                          + "String, String, int, String, String, String)");

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
        push(owner);
        push(name);
        push(desc);
        loadLocal(callIdLocal);
        
        // STACK: ... retval-boxed THIS owner name desc callid

        insertRuntimeCall("void tracePostCall(Object, Object, "
                          + "String, String, String, int)");
      } else {
        // XXX: deal with static invokes.

        // Do the actual method call itself.
        mv.visitMethodInsn(opcode, owner, name, desc);
      }
    }

    public void visitFieldInsn(int opcode,
                               String owner,
                               String name,
                               String desc) {
      if (opcode != Opcodes.GETFIELD ||
          !getFieldIsInteresting(owner, name, desc)) {
        mv.visitFieldInsn(opcode, owner, name, desc);
        return;
      }

      Type receiverType = Misc.getObjectType(owner);
      Type valueType = Type.getType(desc);

      // STACK: receiver

      int receiverLocal = newLocal(receiverType);
      duplicate(receiverType);
      storeLocal(receiverLocal);

      // STACK: receiver
      // Do the actual GETFIELD.
      mv.visitFieldInsn(opcode, owner, name, desc);

      // STACK: value
      
      duplicate(valueType);
      box(valueType);
      loadLocal(receiverLocal);
      push(owner);
      push(name);
      push(desc);

      insertRuntimeCall("void traceFieldRead(Object, Object, String, "
                        + "String, String)");

      // STACK: value
    }

    /**
     * We should really only care about getting fields "across
     * boundaries".  We'll consider getting a field owned by another
     * class (and not a superclass) to be potentially crossing
     * boundaries, for now at least.
     */
    private boolean getFieldIsInteresting(String owner,
                                          String name,
                                          String desc) {
      // XXX: should look at all superclasses, not just immediate.
      // See http://www.objectweb.org/wws/arc/asm/2007-04/msg00015.html
      return !owner.equals(thisClassName) &&
        !owner.equals(thisSuperName);
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