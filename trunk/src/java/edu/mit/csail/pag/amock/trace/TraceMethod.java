package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

import org.objectweb.asm.Type;

import edu.mit.csail.pag.amock.util.*;
import edu.mit.csail.pag.amock.representation.ProgramObject;

public class TraceMethod implements Serializable {
    public final ClassName declaringClass;
    public final String name;
    public final String descriptor;

    public TraceMethod(ClassName declaringClass,
                       String name,
                       String descriptor) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof TraceMethod)) {
            return false;
        }
        TraceMethod t = (TraceMethod) o;

        return declaringClass.equals(t.declaringClass) &&
            name.equals(t.name) &&
            descriptor.equals(t.descriptor);
    }

    @Override public int hashCode() {
        return declaringClass.hashCode() + 3*name.hashCode()
            + 5*descriptor.hashCode();
    }
    
    @Override public String toString() {
        return "[" + declaringClass + "." + name + ":" + descriptor + "]";
    }

    public static TraceMethod createFromMethod(java.lang.reflect.Method m) {
        String className = m.getDeclaringClass().getCanonicalName();
        return new TraceMethod(ClassName.fromDotted(className),
                               m.getName(),
                               Type.getMethodDescriptor(m));
    }

    public boolean isConstructor() {
        return name.equals("<init>");
    }

    public void doUsedAsTypesForArguments(ProgramObject[] args) {
        Type[] argTypes = Type.getArgumentTypes(this.descriptor);
        assert args.length == argTypes.length;
        for (int i = 0; i < args.length; i++) {
            args[i].usedAsType(argTypes[i]);
        }
    }
}
