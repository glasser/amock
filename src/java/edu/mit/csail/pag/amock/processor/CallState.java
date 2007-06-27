package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

public abstract class CallState extends State {
    public CallState(Processor p) {
        super(p);
    }
    
    public void processEvent(TraceEvent ev) {
        if (ev instanceof PreCall) {
            processPreCall((PreCall) ev);
        } else if (ev instanceof PostCall) {
            processPostCall((PostCall) ev);
        } else if (ev instanceof FieldRead) {
            processFieldRead((FieldRead) ev);
        } else if (ev instanceof MethodEntry) {
            processMethodEntry((MethodEntry) ev);
        } else if (ev instanceof MethodExit) {
            processMethodExit((MethodExit) ev);
        }
    }
    abstract public void processPreCall(PreCall p);
    abstract public void processPostCall(PostCall p);
    public void processFieldRead(FieldRead fr) {
        // Do nothing, by default.
    }
    public void processMethodEntry(MethodEntry m) {
        // Do nothing, by default.
    }
    public void processMethodExit(MethodExit m) {
        // Do nothing, by default.
    }
}
