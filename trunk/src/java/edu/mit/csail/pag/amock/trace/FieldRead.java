package edu.mit.csail.pag.amock.trace;

public class FieldRead extends TraceEvent {
    public final Instance receiver;
    public final TraceField field;
    public final TraceObject value;

    public FieldRead(Instance receiver,
                     TraceField field,
                     TraceObject value) {
        this.receiver = receiver;
        this.field = field;
        this.value = value;
    }
}
