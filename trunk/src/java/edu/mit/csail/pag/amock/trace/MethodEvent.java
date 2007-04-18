package edu.mit.csail.pag.amock.trace;

public abstract class MethodEvent extends TraceEvent {
    public final int callId;
    public final TraceMethod method;
    public final TraceObject receiver;

    protected MethodEvent(int callId,
                          TraceMethod method,
                          TraceObject receiver) {
        this.callId = callId;
        this.method = method;
        this.receiver = receiver;
    }

    public boolean isConstructor() {
        return method.name.equals("<init>");
    }
}