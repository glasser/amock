package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

// TESTED MODE inside static method call in JDK or 
// The basic point is to make sure we mark the return value
// as an internal primary.
public class UnmockableStaticMethod extends TestedState {
    public UnmockableStaticMethod(PreCall openingCall,
                                  State continuation,
                                  Processor p) {
        super(openingCall, continuation, p);

        assert this.openingCall.isStatic();
    }

    public void processPostCall(PostCall p) {
        // Processing the end of an ordinary method call.

        // We only care if it's the one that brought us here.
        if (p.callId != openingCall.callId) {
            return;
        }

        TraceObject ret = p.returnValue;

        if (ret instanceof Instance) {
            ClassName cn = ((Instance) ret).className;
                
            boundary().setProgramForTrace(ret,
                                          programObjectFactory()
                                          .addInternalPrimary(cn));
        }

        setState(continuation);
    }
}
