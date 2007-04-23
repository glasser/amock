package edu.mit.csail.pag.amock.trace;

public class FieldRead extends TraceEvent {
    public final TraceObject receiver;
    public final TraceField field;
    public final TraceObject value;

    public FieldRead(TraceObject receiver,
                     TraceField field,
                     TraceObject value) {
        this.receiver = receiver;
        this.field = field;
        this.value = value;
    }
}
