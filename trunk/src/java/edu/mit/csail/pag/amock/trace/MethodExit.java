package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class MethodExit extends MethodEvent {
    public MethodExit(int callId,
                      TraceMethod method) {
        super(callId, method, null, null);
    }
}
