package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

// MOCK MODE initial state
public class WaitForCreation extends PreCallState {
    public WaitForCreation(Processor p) {
        super(p);
    }
    
    public void processPreCall(PreCall p) {
        if (!(p.method.declaringClass.equals(getTestedClass())
              && p.isConstructor())) {
            return;
        }

        setState(new TestedModeMain(p,
                                    null,
                                    new MockModeWaiting(getProcessor()),
                                    true,
                                    getProcessor()));
    }
}
