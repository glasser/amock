package edu.mit.csail.pag.amock.trace;

public class PostCall extends TraceEvent {
    private final int callId;
    private final TraceMethod method;
    private final TraceObject receiver;
    private final TraceObject[] args;
    private final TraceObject returnValue;

    public PostCall(int callId,
                    TraceMethod method,
                    TraceObject receiver,
                    TraceObject[] args,
                    TraceObject returnValue) {
        this.callId = callId;
        this.method = method;
        this.receiver = receiver;
        this.args = args;
        this.returnValue = returnValue;
    }
}
