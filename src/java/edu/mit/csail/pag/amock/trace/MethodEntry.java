package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class MethodEntry extends TraceEvent implements Serializable {
    public final int callId;
    public final TraceMethod method;
//     public final TraceObject receiver;
//     public final TraceObject[] args;

    public MethodEntry(int callId,
                       TraceMethod method) {
        this.callId = callId;
        this.method = method;
    }
}
