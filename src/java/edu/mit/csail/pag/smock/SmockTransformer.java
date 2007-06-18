package edu.mit.csail.pag.smock;

import edu.mit.csail.pag.amock.trace.CustomGeneratorAdapter;

import java.util.*;

// Note: ASM 2 is required.
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class SmockTransformer extends ClassAdapter {
    private String className;
    
    public SmockTransformer(ClassVisitor cv) {
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

    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc,
                                          signature, exceptions);
        
        // If for some reason the next ClassVisitor down the chain thinks
        // this method isn't worth visiting, don't visit it.
        if (mv == null) {
            return null;
        }

        // Don't instrument weird stuff.
        if (name.equals("<clinit>") || name.equals("<init>")
            || name.contains("$")) {
            return mv;
        }

        // Only instrument static methods.
        if ((access & Opcodes.ACC_STATIC) == 0) {
            return mv;
        }

        return new SmockMethodTransformer(mv, className, access, name, desc);
    }

    public static class SmockMethodTransformer extends CustomGeneratorAdapter {
        private final String className;
        private final String name;
        private final String desc;

        public SmockMethodTransformer(MethodVisitor mv,
                                      String className,
                                      int access,
                                      String name,
                                      String desc) {
            super(mv, access, name, desc);
            this.className = className;
            this.name = name;
            this.desc = desc;
        }

        private static final Type SMOCK_TYPE =
            Type.getType(Smock.class);
        private static final Type RESULT_TYPE =
            Type.getType(Result.class);
        private static final Type OBJECT_TYPE =
            Type.getType(Object.class);

        /**
         * Insert a call to the method on the runtime class described by
         * javaDesc; the arguments must already be on the stack.
         */
        private void insertRuntimeCall(String javaDesc) {
            // TODO: Cache Method lookups.
            Method m = Method.getMethod(javaDesc);
            invokeStatic(SMOCK_TYPE, m);
        }

        /**
         * Instrument method entry by calling Smock.maybeMockStaticMethod.
         */
        public void visitCode() {
            mv.visitCode();
            
            Type[] argTypes = Type.getArgumentTypes(desc);
            Type returnType = Type.getReturnType(this.desc);

            if (returnType.getSort() == Type.VOID) {
                return; // XXX should instrument anyway
            }

            int[] argLocals = getArrayOfArguments(desc);
            push(className);
            push(name);
            push(desc);
            pushArrayOfLocals(argLocals);
            insertRuntimeCall("edu.mit.csail.pag.smock.Result maybeMockStaticMethod(String, String, String, Object[])");
            dup();

            // STACK: result result
            getField(RESULT_TYPE, "shortCircuit", Type.BOOLEAN_TYPE);

            Label after = newLabel();
            // STACK: result shortCircuit?
            ifZCmp(EQ, after);

            // STACK: result
            // ... and we should shortCircuit
            getField(RESULT_TYPE, "shortCircuitReturnValue", OBJECT_TYPE);
            unbox(returnType);
            returnValue();

            mark(after);
            // STACK: result
            // Drop result and get arguments back.
            pop();
            pushLocalsFromArray(argLocals);
        }
    }
}
