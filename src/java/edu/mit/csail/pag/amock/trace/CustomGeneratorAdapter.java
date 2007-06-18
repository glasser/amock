package edu.mit.csail.pag.amock.trace;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class CustomGeneratorAdapter extends GeneratorAdapter {
    public CustomGeneratorAdapter(MethodVisitor mv,
                                  int access,
                                  String name,
                                  String desc) {
        super(mv, access, name, desc);
    }
    
    private static final Type OBJECT_TYPE = Type.getType(Object.class);
    
    /**
     * Assembles either DUP or DUP2, depending on the size of the
     * type.
     */
    protected void duplicate(Type t) {
        if (t.getSize() == 2) {
            dup2();
        } else {
            dup();
        }
    }

    protected int[] getArrayOfLocals(String desc) {
        Type[] argTypes = Type.getArgumentTypes(desc);
      
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

        return argLocals;
    }

    /**
     * Pushes an array containing the values of the locals in
     * someLocals, which must have been created with newLocal.
     */
    protected void pushArrayOfLocals(int[] someLocals) {
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