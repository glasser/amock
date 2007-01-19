package edu.mit.csail.pag.amock.trace;

public class TraceMethod {
    private final String declaringClass;
    private final String name;
    private final String descriptor;

    public TraceMethod(String declaringClass,
                       String name,
                       String descriptor) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.descriptor = descriptor;
    }
}