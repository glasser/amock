package edu.mit.csail.pag.amock.trace;

public class MethodEntry extends MethodEvent {
    public MethodEntry(int callId,
                       TraceMethod method,
                       TraceObject receiver) {
        super(callId, method, receiver, null);
    }
}
