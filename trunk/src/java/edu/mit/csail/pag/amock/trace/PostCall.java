package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class PostCall extends TraceEvent implements Serializable {
    public final int callId;
    public final TraceMethod method;
    public final TraceObject receiver;
    public final TraceObject[] args;
    public final TraceObject returnValue;

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

    public boolean isConstructor() {
        return method.name.equals("<init>");
    }

}
