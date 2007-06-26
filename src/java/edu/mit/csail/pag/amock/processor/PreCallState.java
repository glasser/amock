package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

public abstract class PreCallState extends State {
    public PreCallState(Processor p) {
        super(p);
    }
    
    public void processEvent(TraceEvent ev) {
        if (!(ev instanceof PreCall)) {
            return;
        }

        processPreCall((PreCall) ev);
    }
    abstract public void processPreCall(PreCall p);
}
