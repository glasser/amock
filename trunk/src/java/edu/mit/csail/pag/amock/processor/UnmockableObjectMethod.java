package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

// TESTED MODE inside non-overridable method (equals, hashCode, etc).
public class UnmockableObjectMethod extends TestedState {
    public UnmockableObjectMethod(PreCall openingCall,
                                  State continuation,
                                  Processor p) {
        super(openingCall, continuation, p);

        assert this.openingCall.method.name.equals("equals") ||
            this.openingCall.method.name.equals("hashCode");
    }

    public void processPostCall(PostCall p) {
        // We only care if it's the one that brought us here.
        if (p.callId != openingCall.callId) {
            return;
        }
        
        setState(continuation);
    }
}
