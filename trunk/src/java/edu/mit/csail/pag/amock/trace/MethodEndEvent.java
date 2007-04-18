package edu.mit.csail.pag.amock.trace;

/**
 * Currently this is a base class for both just-made-a-call (PostCall)
 * and about-to-return (MethodExit) events.  In theory these two types
 * are redundant but when calling uninstrumented code or being called
 * from uninstrumented code, you only get one.
 */
public abstract class MethodEndEvent extends MethodEvent {
    public MethodEndEvent(int callId,
                          TraceMethod method,
                          TraceObject receiver) {
        super(callId, method, receiver);
    }
}
