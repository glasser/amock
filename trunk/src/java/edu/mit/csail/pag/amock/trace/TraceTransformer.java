package edu.mit.csail.pag.amock.trace;

import java.util.*;

import java.lang.reflect.Method;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;


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

    return new TraceMethodTransformer(access, desc, mv);
  }

  public static class TraceMethodTransformer extends LocalVariablesSorter {
    public TraceMethodTransformer(int access, String desc, MethodVisitor mv) {
      super(access, desc, mv);
    }

    /**
     * Instrument method calls.
     */
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc) {
      if (opcode == Opcodes.INVOKEVIRTUAL) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        Type returnType = Type.getReturnType(desc);
        Type receiverType = Type.getObjectType(owner);

        // Allocate locals and save argument values into them.
        // TODO: optimize by reusing locals across different
        // instrumentations of the same method.
        int[] argumentLocals = new int[argumentTypes.length];

        for (int i = 0; i < argumentTypes.length; i++) {
          Type t = argumentTypes[i];
          int argumentLocal = newLocal(t);
          argumentLocals[i] = argumentLocal;
          mv.visitVarInsn(t.getOpcode(Opcodes.ISTORE),
                          argumentLocal);
        }

        // Save the receiver into a local (but keep it on the stack);
        int receiverLocal = newLocal(receiverType);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(receiverType.getOpcode(Opcodes.ISTORE),
                        receiverLocal);

        // Now push the arguments back onto the stack.
        for (int i = 0; i < argumentTypes.length; i++) {
          mv.visitVarInsn(argumentTypes[i].getOpcode(Opcodes.ILOAD),
                          argumentLocals[i]);
        }

        // Get a call ID from the TraceRuntime class.
        insertRuntimeCall("get_call_id");
      }
      // XXX: deal with static, special, and interface invokes.

      // Do the actual method call itself.
      mv.visitMethodInsn(opcode, owner, name, desc);
    }

    // The class which trace calls get sent to.
    private static final Class traceRuntimeClass = TraceRuntime.class;

    // The methods from the TraceRuntime class, reachable by name.  We
    // assume that the methods that we actually call are not
    // overloaded --- only one method of a given name exists.
    private static final Map<String, Method> traceRuntimeMethods =
      new HashMap<String, Method>();
    static {
      for (Method m : traceRuntimeClass.getDeclaredMethods()) {
        traceRuntimeMethods.put(m.getName(), m);
      }
    }

    private void insertRuntimeCall(String name) {
      Method m = traceRuntimeMethods.get(name);
      if (m == null) {
        throw new RuntimeException("Unknown method in TraceRuntime class: "
                                   + name);
      }

      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                         Type.getInternalName(traceRuntimeClass),
                         name,
                         Type.getMethodDescriptor(m));
    }
  }
    
}