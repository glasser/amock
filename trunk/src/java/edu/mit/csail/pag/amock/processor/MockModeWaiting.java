package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

// MOCK MODE idle state
public class MockModeWaiting extends PreCallState {
    public MockModeWaiting(Processor p) {
        super(p);
    }
    
    public void processPreCall(PreCall p) {
        if (p.isConstructor()) {
            // We don't care about random things being
            // constructed.
                
            // TODO: this is assuming that only one explicit
            // primary ever gets constructed; this should really
            // be up to the BoundaryTranslator.
            return;
        }
            
        if (!(boundary().isKnownPrimary(p.receiver))) {
            return;
        }

        Primary receiverPrimary = (Primary) getProgramObject(p.receiver);

        PrimaryExecution primaryExecution =
            programObjectFactory().addPrimaryExecution(receiverPrimary,
                                                       p.method,
                                                       getProgramObjects(p.args));

        setState(new TestedModeMain(p,
                                    primaryExecution,
                                    new MockModeWaiting(getProcessor()),
                                    false,
                                    getProcessor()));
    }
}
