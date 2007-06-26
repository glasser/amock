package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

// This state occurs if, in tested mode, a method is invoked on a
// "record type" object.  Basically, it tracks the return and
// tells the RecordPrimary about it so that it can set up
// constructor calls properly.  (Really, it should provide an
// "escape" that allows it to turn the RecordPrimary into a Mocked
// if anything too complicated happens.)
public class RecordPrimaryInvocation extends PostCallState {
    private final PreCall openingCall;
    private final State continuation;
    private final RecordPrimary receiver;

    public RecordPrimaryInvocation(PreCall openingCall,
                                   State continuation,
                                   Processor proc) {
        super(proc);
        this.openingCall = openingCall;
        this.continuation = continuation;

        ProgramObject p = getProgramObject(openingCall.receiver);

        assert p instanceof RecordPrimary;
        receiver = (RecordPrimary) p;
    }

    public void processPostCall(PostCall p) {
        if (p.callId != openingCall.callId) {
            // TODO: maybe this was a callback?
            return;
        }

        TraceObject ret = p.returnValue;

        if (ret instanceof VoidReturnValue) {
            // Do nothing.
        } else {
            ProgramObject m = getProgramObject(ret);

            receiver.returnsFromMethod(openingCall.method,
                                       m);
        }
            
        setState(continuation);
    }
}
