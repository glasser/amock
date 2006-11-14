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
    
    /**
     * Instrument method calls.
     */
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc) {
      if (opcode == Opcodes.INVOKEVIRTUAL) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        Type returnType = Type.getReturnType(desc);
        Type receiverType = Type.getObjectType(owner);

        // STACK: ... this args

        // Allocate locals and save argument values into them.
        // TODO: optimize by reusing locals across different
        // instrumentations of the same method.
        int[] argumentLocals = new int[argumentTypes.length];

        // Iterate backwards, since the last argument is on top of the
        // stack.
        for (int i = argumentTypes.length - 1; i >= 0; i--) {
          int argumentLocal = newLocal(argumentTypes[i]);
          argumentLocals[i] = argumentLocal;
          storeLocal(argumentLocal);
        }

        // STACK: ... this

        // Save the receiver into a local (but keep it on the stack);
        int receiverLocal = newLocal(receiverType);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(receiverType.getOpcode(Opcodes.ISTORE),
                        receiverLocal);

        // STACK: ... this

        // Now push the arguments back onto the stack.
        for (int i = 0; i < argumentTypes.length; i++) {
          mv.visitVarInsn(argumentTypes[i].getOpcode(Opcodes.ILOAD),
                          argumentLocals[i]);
        }

        // STACK: ... this args

        // Get a call ID from the TraceRuntime class.
        int callIdLocal = newLocal(Type.INT_TYPE);
        insertRuntimeCall("int get_call_id()");
        dup();
        storeLocal(callIdLocal);
        loadLocal(receiverLocal);
        
        // STACK: ... this args callid this

        // Put an array containing the arguments on the stack.
        // First, make an empty array of the right size:
        assert argumentLocals.length >= Byte.MIN_VALUE &&
          argumentLocals.length <= Byte.MAX_VALUE;
        push(argumentLocals.length);
        newArray(OBJECT_TYPE);
        
        for (int i = 0; i < argumentLocals.length; i++) {
          // Duplicate the array reference.
          dup();
          push(i);
          loadLocal(argumentLocals[i]);
          // This is an Object array, so box the value if needed.
          box(argumentTypes[i]);
          arrayStore(OBJECT_TYPE);
        }

        push(desc);

        // STACK: ... this args callid this [args] desc

        insertRuntimeCall("void enter(int, Object, Object[], String)");

        // STACK: ... this args
        // Ready to make real call.
      }
      // xxx: deal with static, special, and interface invokes.

      // Do the actual method call itself.
      mv.visitMethodInsn(opcode, owner, name, desc);
    }

    // The class which trace calls get sent to.
    private static final Type traceRuntimeType =
      Type.getType(TraceRuntime.class);

    private void insertRuntimeCall(String javaDesc) {
      // TODO: Cache Method lookups.
      Method m = Method.getMethod(javaDesc);
      invokeStatic(traceRuntimeType, m);
    }
  }
    
}