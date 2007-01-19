package edu.mit.csail.pag.amock.trace;

public class PreCall extends TraceEvent {
    private final int callId;
    private final TraceMethod method;
    private final TraceObject receiver;
    private final TraceObject[] args;

    public PreCall(int callId,
                   TraceMethod method,
                   TraceObject receiver,
                   TraceObject[] args) {
        this.callId = callId;
        this.method = method;
        this.receiver = receiver;
        this.args = args;
    }
}
