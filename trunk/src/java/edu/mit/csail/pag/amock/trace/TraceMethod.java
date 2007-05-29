package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

import org.objectweb.asm.Type;

import edu.mit.csail.pag.amock.util.Misc;

public class TraceMethod implements Serializable {
    public final String declaringClass;
    public final String name;
    public final String descriptor;

    public TraceMethod(String declaringClass,
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
        return new TraceMethod(Misc.classNamePeriodsToSlashes(className),
                               m.getName(),
                               Type.getMethodDescriptor(m));
    }
}
