package edu.mit.csail.pag.amock.trace;

public abstract class MethodEvent extends TraceEvent {
    public final int callId;
    public final TraceMethod method;
    // receiver is null (not a Primitive wrapping null) for static
    // calls.
    public final TraceObject receiver;

    protected MethodEvent(int callId,
                          TraceMethod method,
                          TraceObject receiver) {
        this.callId = callId;
        this.method = method;
        if (receiver instanceof Primitive
            && ((Primitive)receiver).value == null) {
            this.receiver = null;
        } else {
            this.receiver = receiver;
        }
    }

    public boolean isConstructor() {
        return method.isConstructor();
    }

    public boolean isStatic() {
        return receiver == null;
    }
}