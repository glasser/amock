package edu.mit.csail.pag.amock.trace;

public class MethodEntry extends MethodStartEvent {
    public MethodEntry(int callId,
                       TraceMethod method,
                       TraceObject receiver,
                       TraceObject[] args) {
        super(callId, method, receiver, args);
    }
}
