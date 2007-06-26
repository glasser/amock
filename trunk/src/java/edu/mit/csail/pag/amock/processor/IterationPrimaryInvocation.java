package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;


// This state occurs if, in tested mode, a method is invoked on an
// "iteration pattern" object.  Basically, if it's "next", then we
// see what it returns and add that to the construction of the
// iterator; otherwise (it's something like hasNext) we ignore it.
// (We know it can only be these things since otherwise it
// wouldn't have been selected as IterationPrimary.)
public class IterationPrimaryInvocation extends PostCallState {
    private final PreCall openingCall;
    private final State continuation;
    private final IterationPrimary receiver;

    public IterationPrimaryInvocation(PreCall openingCall,
                                      State continuation,
                                      Processor proc) {
        super(proc);
        this.openingCall = openingCall;
        this.continuation = continuation;

        ProgramObject p = getProgramObject(openingCall.receiver);

        assert p instanceof IterationPrimary;
        receiver = (IterationPrimary) p;
    }

    public void processPostCall(PostCall p) {
        if (p.callId != openingCall.callId) {
            // TODO: maybe this was a callback?
            return;
        }

        TraceObject ret = p.returnValue;

        if (!(ret instanceof VoidReturnValue)) {
            ProgramObject retPO = getProgramObject(ret);

            receiver.returnsFromMethod(openingCall.method,
                                       retPO);
        }
            
        setState(continuation);
    }
}
