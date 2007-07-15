package edu.mit.csail.pag.amock.trace;

public class PreCall extends MethodStartEvent {
    // This flag is true if the call is a constructor and it's the
    // first one called (not a superclass constructor or this(x)
    // call).  Note that the tracer does not actually set this
    // correctly; the ConstructorFixer does.
    //
    // (Also, for the special case of java.lang.String or a boxed
    // primitive's constructor, this should always be false.)
    public final boolean isTopLevelConstructor;
    // calledFrom is only used for debugging reasons, and should
    // probably be cut when doing efficiency tests.
    public final TraceMethod calledFrom;

    public PreCall(int callId,
                   TraceMethod method,
                   TraceObject receiver,
                   TraceObject[] args,
                   TraceMethod calledFrom,
                   boolean isTopLevelConstructor) {
        super(callId, method, receiver, args);
        this.isTopLevelConstructor = isTopLevelConstructor;
        this.calledFrom = calledFrom;

        if (isTopLevelConstructor) {
            assert isConstructor();
        }
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
                           this.calledFrom,
                           newTLC);
    }
}
