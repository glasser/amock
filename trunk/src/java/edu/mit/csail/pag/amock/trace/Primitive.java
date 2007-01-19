package edu.mit.csail.pag.amock.trace;

public class Primitive extends TraceObject {
    private final Object value;

    public Primitive(Object value) {
        this.value = value;
    }
}
