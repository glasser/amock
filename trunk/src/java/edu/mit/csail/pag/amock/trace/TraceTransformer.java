package edu.mit.csail.pag.amock.trace;

import java.util.*;

// Note: ASM 2 is required.
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

import edu.mit.csail.pag.amock.util.*;

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
    
    boolean isPublic = (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    
    hierarchyDump.write(HierarchyEntry.fromSlashed(className,
                                                   superName,
                                                   interfaces,
                                                   isPublic));
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

    // Instrument static init in a much simpler way.
    if (name.equals("<clinit>")) {
      return new ClinitTransformer(mv, access, className, name, desc);
    }

    return new TraceMethodTransformer(mv, access, className, superName,
                                      name, desc);
  }

  /**
   * A MethodVisitor which adds tracing calls to the method it is
   * visiting.
   */
  public static class TraceMethodTransformer extends CustomGeneratorAdapter {
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

    // The Type of java.lang.Class.
    private static final Type CLASS_TYPE = Type.getType(Class.class);

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
          opcode == Opcodes.INVOKEINTERFACE ||
          opcode == Opcodes.INVOKESTATIC) {
        boolean isStatic = (opcode == Opcodes.INVOKESTATIC);
        Type returnType = Type.getReturnType(desc);
        Type receiverType = ClassName.fromSlashed(owner).getObjectType();

        // set to non-null only if we're pretending that a
        // Class.newInstance() is a constructor.
        Integer classNameLocal = null;

        // STACK: ... this args

        // Note: in the case of a constructor call, the "this" might
        // be an uninitialized object; the JVM won't allow us to store
        // them into local variables.  In "STACK" diagrams, "this"
        // will indicate a possibly-uninitialized receiver, "THIS" a
        // definitely-initialized one, and "this!" either a receiver
        // or the CONSTRUCTOR_RECEIVER object which replaces it.  All
        // these variants of "this" don't exist is it's a static
        // method.

        // If it's really Class.newInstance(), pretend it's a
        // constructor.
        if (owner.equals("java/lang/Class") &&
            name.equals("newInstance") &&
            desc.equals("()Ljava/lang/Object;")) {
          assert !isStatic;
          // We know now that in fact, the stack is
          // STACK: THIS
          // Get the class name...

          Method m = Method.getMethod("String getCanonicalName()");
          duplicate(receiverType);
          // STACK: THIS THIS
          invokeVirtual(CLASS_TYPE, m);
          // STACK: THIS className
          classNameLocal = newLocal(Type.getType(String.class));
          storeLocal(classNameLocal);
          // STACK: THIS
        }

        boolean isConstructorCall = name.equals("<init>")
          || classNameLocal != null;

        int[] argLocals = getArrayOfArgumentsFromStack(desc);
        
        // STACK: ... this

        // Save the receiver (or something representing it) into a
        // local (but keep it on the stack).
        int receiverLocal = 0; // protect against uninitialized warning only

        if (!isStatic) {
          receiverLocal = newLocal(receiverType);
          if (isConstructorCall) {
            getStatic(TRACE_RUNTIME_TYPE,
                      "CONSTRUCTOR_RECEIVER",
                      OBJECT_TYPE);
          } else {
            duplicate(receiverType);
          }

          // STACK: ... this this!
          storeLocal(receiverLocal);
        }

        // Get a call ID from the Tracer class.
        int callIdLocal = newLocal(Type.INT_TYPE);
        insertRuntimeCall("int getNextCallId()");
        storeLocal(callIdLocal);

        // STACK: ... this

        // Set up the arguments for tracePreCall.
        if (isStatic) {
          push((String) null);
        } else {
          loadLocal(receiverLocal);
        }
        pushArrayOfLocals(argLocals);
        if (classNameLocal == null) {
          push(owner);
          push(name);
          push(desc);
        } else {
          loadLocal(classNameLocal);
          push("<<init>>"); // hack to trigger .-to-/ in tracePreCall
          push("()V");
        }
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
        if (isConstructorCall) {
          duplicate(receiverType);
          storeLocal(receiverLocal);
        }

        pushLocalsFromArray(argLocals);

        // STACK: ... THIS args
        
        // Actually make the method call.
        mv.visitMethodInsn(opcode, owner, name, desc);

        if (classNameLocal == null) {
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
          if (isStatic) {
            push((String) null);
          } else {
            loadLocal(receiverLocal);
          }
          push(owner);
          push(name);
          push(desc);
        } else {
          // OK.  The stack currently contains the newly created
          // instance.  We want this to look like the *receiver* and
          // make the return value be void.
          duplicate(OBJECT_TYPE);
          getStatic(TRACE_RUNTIME_TYPE,
                    "VOID_RETURN_VALUE",
                    OBJECT_TYPE);
          swap(OBJECT_TYPE, OBJECT_TYPE);
          loadLocal(classNameLocal);
          push("<<init>>"); // hack to trigger .-to-/ in tracePostCall
          push("()V");
        }

        loadLocal(callIdLocal);
        
        // STACK: ... retval-boxed THIS owner name desc callid

        insertRuntimeCall("void tracePostCall(Object, Object, "
                          + "String, String, String, int)");
      } else {
        // Do the actual method call itself.
        mv.visitMethodInsn(opcode, owner, name, desc);
      }
    }

    public void visitFieldInsn(int opcode,
                               String owner,
                               String name,
                               String desc) {
      if ((opcode != Opcodes.GETFIELD && opcode != Opcodes.GETSTATIC) ||
          !getFieldIsInteresting(owner, name, desc)) {
        mv.visitFieldInsn(opcode, owner, name, desc);
        return;
      }

      boolean isStatic = opcode == Opcodes.GETSTATIC;

      Type receiverType = ClassName.fromSlashed(owner).getObjectType();
      Type valueType = Type.getType(desc);
      int receiverLocal = newLocal(receiverType);

      if (! isStatic) {
        // STACK: receiver

        duplicate(receiverType);
        storeLocal(receiverLocal);
      }

      // Do the actual operation.
      mv.visitFieldInsn(opcode, owner, name, desc);

      // STACK: value
      
      duplicate(valueType);
      box(valueType);
      if (isStatic) {
        push((String)null);
      } else {
        loadLocal(receiverLocal);
      }
      push(owner);
      push(name);
      push(desc);
      push(isStatic);

      insertRuntimeCall("void traceFieldRead(Object, Object, String, "
                        + "String, String, boolean)");

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
  }

  /**
   * A MethodVisitor which adds tracing calls to the method it is
   * visiting.
   */
  public static class ClinitTransformer extends CustomGeneratorAdapter {
    private final String thisClassName;
    
    // This local is used to store a call ID for the clinit itself.
    private final int methodCallIdLocal = newLocal(Type.INT_TYPE);
    
    public ClinitTransformer(MethodVisitor mv,
                             int access,
                             String thisClassName,
                             String thisName,
                             String thisDesc) {
      super(mv, access, thisName, thisDesc);
      this.thisClassName = thisClassName;
    }

    // The Type of java.lang.Object; cached as it is used several
    // times.
    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    // The Type of java.lang.Class.
    private static final Type CLASS_TYPE = Type.getType(Class.class);

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
     * Instrument method entry to note that we are in a clinit.
     */
    public void visitCode() {
      mv.visitCode();

      // Get a call ID for the method itself.
      insertRuntimeCall("int getNextCallId()");
      storeLocal(methodCallIdLocal);
      
      push(thisClassName);
      loadLocal(methodCallIdLocal);
      insertRuntimeCall("void clinitEntry(String, int)");
    }

    /**
     * Instrument return instructions to show that we're done with
     * clinit.  (Let's just hope we don't leave clinit via
     * exception...)
     */
    public void visitInsn(int opcode) {
      if (opcode == Opcodes.IRETURN ||
          opcode == Opcodes.LRETURN ||
          opcode == Opcodes.FRETURN ||
          opcode == Opcodes.DRETURN ||
          opcode == Opcodes.ARETURN ||
          opcode == Opcodes.RETURN) {
        push(thisClassName);
        loadLocal(methodCallIdLocal);
        insertRuntimeCall("void clinitExit(String, int)");
      }

      // Do the actual instruction.
      mv.visitInsn(opcode);
    }
  }
}
