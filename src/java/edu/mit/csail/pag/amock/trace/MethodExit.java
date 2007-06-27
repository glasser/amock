package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class MethodExit extends MethodEndEvent {
    public MethodExit(int callId,
                      TraceMethod method,
                      TraceObject receiver,
                      TraceObject returnValue) {
        super(callId, method, receiver, returnValue);
    }
}
