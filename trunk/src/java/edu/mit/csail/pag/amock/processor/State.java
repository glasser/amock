package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

public abstract class State implements TraceProcessor<TraceEvent> {
    private final Processor p;
    public State(Processor p) {
        this.p = p;
    }

    public Processor getProcessor() {
        return this.p;
    }

    public void setState(State newState) {
        p.setState(newState);
    }

    public ProgramObject getProgramObject(TraceObject t) {
        return p.getProgramObject(t);
    }

    public ProgramObject getProgramObjectForReturnAction(TraceObject t) {
        return p.getProgramObjectForReturnAction(t);
    }

    public ProgramObject[] getProgramObjects(TraceObject[] tos) {
        return p.getProgramObjects(tos);
    }

    public ClassName getTestedClass() {
        return p.getTestedClass();
    }

    public BoundaryTranslator boundary() {
        return p.boundary();
    }

    public ProgramObjectFactory programObjectFactory() {
        return p.programObjectFactory();
    }

    public void switchInto() {
        //        System.err.println(getClass().getSimpleName());
    }
}
