package edu.mit.csail.pag.amock.trace;

import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;


public class TraceTransformer extends ClassAdapter {
  public TraceTransformer(ClassVisitor cv) {
    super(cv);
  }

  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions) {
    MethodVisitor mv = cv.visitMethod(access, name, desc,
                                      signature, exceptions);

    // If for some reason the next ClassVisitor down the chain thinks
    // this method isn't worth visiting, don't visit it.
    if (mv == null) {
      return null;
    }

    return new TraceMethodTransformer(mv, access, name, desc);
  }

  public static class TraceMethodTransformer extends GeneratorAdapter {
    public TraceMethodTransformer(MethodVisitor mv,
                                  int access,
                                  String name,
                                  String desc) {
      super(mv, access, name, desc);
    }

    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    // The class which trace calls get sent to.
    private static final Type traceRuntimeType =
      Type.getType(Tracer.class);

    private void insertRuntimeCall(String javaDesc) {
      // TODO: Cache Method lookups.
      Method m = Method.getMethod(javaDesc);
      invokeStatic(traceRuntimeType, m);
    }

    /**
     * Instrument method calls.
     */
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc) {
      if (opcode == Opcodes.INVOKEVIRTUAL) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        Type returnType = Type.getReturnType(desc);
        Type receiverType = Type.getObjectType(owner);

        // STACK: ... this args

        // Allocate locals and save argument values into them.
        // TODO: optimize by reusing locals across different
        // instrumentations of the same method.
        int[] argLocals = new int[argTypes.length];

        // Iterate backwards, since the last argument is on top of the
        // stack.
        for (int i = argTypes.length - 1; i >= 0; i--) {
          int argumentLocal = newLocal(argTypes[i]);
          argLocals[i] = argumentLocal;
          storeLocal(argumentLocal);
        }

        // STACK: ... this

        // Save the receiver into a local (but keep it on the stack);
        int receiverLocal = newLocal(receiverType);
        dup();
        storeLocal(receiverLocal);

        // STACK: ... this

        // Now push the arguments back onto the stack.
        for (int local : argLocals) {
          loadLocal(local);
        }

        // STACK: ... this args

        // Get a call ID from the Tracer class.
        int callIdLocal = newLocal(Type.INT_TYPE);
        insertRuntimeCall("int getCallId()");
        dup();
        storeLocal(callIdLocal);
        loadLocal(receiverLocal);
        
        // STACK: ... this args callid this

        // Put an array containing the arguments on the stack.
        pushArrayOfLocals(argLocals);

        push(owner);
        push(name);
        push(desc);

        // STACK: ... this args callid this [args] owner name desc

        insertRuntimeCall("void tracePreCall(int, Object, Object[], String, "
                          + "String, String)");

        // STACK: ... this args
        // Actually make the method call.
        mv.visitMethodInsn(opcode, owner, name, desc);

        if (returnType.getSort() == Type.VOID) {
          getStatic(traceRuntimeType,
                    "VOID_RETURN_VALUE",
                    OBJECT_TYPE);
        } else if (returnType.getSize() == 2) {
          dup2();
        } else {
          dup();
        }
        box(returnType);

        loadLocal(receiverLocal);

        // STACK: ... retval-copy this

        pushArrayOfLocals(argLocals);
        push(owner);
        push(name);
        push(desc);
        loadLocal(callIdLocal);
        
        // STACK: ... retval-copy this [args] owner name desc callid

        insertRuntimeCall("void tracePostCall(Object, Object, Object[], "
                          + "String, String, String, int)");
      } else {
        // xxx: deal with static, special, and interface invokes.

        // Do the actual method call itself.
        mv.visitMethodInsn(opcode, owner, name, desc);
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