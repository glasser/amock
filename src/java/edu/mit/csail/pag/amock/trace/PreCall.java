package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class PreCall extends TraceEvent implements Serializable {
    public final int callId;
    public final TraceMethod method;
    public final TraceObject receiver;
    public final TraceObject[] args;

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
