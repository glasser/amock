package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

import org.objectweb.asm.Type;

import edu.mit.csail.pag.amock.util.*;

public class TraceField implements Serializable {
    public final ClassName declaringClass;
    public final String name;
    public final String descriptor;

    public TraceField(ClassName declaringClass,
                      String name,
                      String descriptor) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof TraceField)) {
            return false;
        }
        TraceField t = (TraceField) o;

        return declaringClass.equals(t.declaringClass) &&
            name.equals(t.name) &&
            descriptor.equals(t.descriptor);
    }

    @Override public int hashCode() {
        return 5*declaringClass.hashCode() + 3*name.hashCode()
            + 7*descriptor.hashCode();
    }

    @Override public String toString() {
        return "[" + declaringClass + "." + name + ":" + descriptor + "]";
    }

    public static TraceField createFromField(java.lang.reflect.Field f) {
        String className = f.getDeclaringClass().getCanonicalName();
        return new TraceField(ClassName.fromDotted(className),
                              f.getName(),
                              Type.getDescriptor(f.getType()));
    }
}
