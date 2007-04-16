package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class PreCall extends TraceEvent implements Serializable {
    public final int callId;
    public final TraceMethod method;
    public final TraceObject receiver;
    public final TraceObject[] args;
    // This flag is true if the call is a constructor and it's the
    // first one called (not a superclass constructor or this(x)
    // call).  Note that the tracer does not actually set this
    // correctly; the ConstructorFixer does.
    public final boolean isTopLevelConstructor;

    public PreCall(int callId,
                   TraceMethod method,
                   TraceObject receiver,
                   TraceObject[] args,
                   boolean isTopLevelConstructor) {
        this.callId = callId;
        this.method = method;
        this.receiver = receiver;
        this.args = args;
        this.isTopLevelConstructor = isTopLevelConstructor;

        if (isTopLevelConstructor) {
            assert isConstructor();
        }
    }

    public boolean isConstructor() {
        return method.name.equals("<init>");
    }

    /**
     * This is used by the ConstructorFixer to splice in the correct
     * receiver.
     */
    public PreCall copyWithNewReceiverAndTLCFlag(TraceObject newReceiver,
                                                 boolean newTLC) {
        return new PreCall(this.callId,
                           this.method,
                           newReceiver,
                           this.args,
                           newTLC);
    }
}
