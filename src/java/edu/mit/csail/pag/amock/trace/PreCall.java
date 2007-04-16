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

    public boolean isConstructor() {
        return method.name.equals("<init>");
    }

    /**
     * This is used by the ConstructorFixer to splice in the correct
     * receiver.
     */
    public PreCall copyWithNewReceiver(TraceObject newReceiver) {
        return new PreCall(this.callId,
                           this.method,
                           newReceiver,
                           this.args);
    }
}
