package edu.mit.csail.pag.amock.trace;

public class Instance extends TraceObject {
    private final String className;
    private final int id;

    public Instance(String className, int id) {
        this.className = className;
        this.id = id;
    }
}
