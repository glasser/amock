package edu.mit.csail.pag.amock.trace;

public class TraceMethod {
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
}