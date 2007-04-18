package edu.mit.csail.pag.amock.trace;

public class PostCall extends MethodEndEvent {
    public final TraceObject returnValue;

    public PostCall(int callId,
                    TraceMethod method,
                    TraceObject receiver,
                    TraceObject returnValue) {
        super(callId, method, receiver);
        this.returnValue = returnValue;
    }
}
