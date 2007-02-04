package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

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
}