package edu.mit.csail.pag.amock.trace;

/**
 * Currently this is a base class for both about-to-make-a-call
 * (PreCall) and just-started-a-method (MethodEntry) events.  In
 * theory these two types are redundant but when calling
 * uninstrumented code or being called from uninstrumented code, you
 * only get one.
 */
public abstract class MethodStartEvent extends MethodEvent {
    public final TraceObject[] args;

    protected MethodStartEvent(int callId,
                               TraceMethod method,
                               TraceObject receiver,
                               TraceObject[] args) {
        super(callId, method, receiver);
        this.args = args;
    }
}
