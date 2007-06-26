package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

public abstract class PostCallState extends State {
    public PostCallState(Processor p) {
        super(p);
    }
    
    public void processEvent(TraceEvent ev) {
        if (!(ev instanceof PostCall)) {
            return;
        }

        processPostCall((PostCall) ev);
    }
    abstract public void processPostCall(PostCall p);
}
