package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

import java.util.*;

public class SingleObjectBoundaryTranslator implements BoundaryTranslator {
    private final TestMethodGenerator testMethodGenerator;
    private final Map<TraceObject, ProgramObject> knownMappings =
        new HashMap<TraceObject, ProgramObject>();

    public SingleObjectBoundaryTranslator(TestMethodGenerator testMethodGenerator) {
        this.testMethodGenerator = testMethodGenerator;
    }

    public boolean isKnownPrimary(TraceObject t) {
        return knownMappings.containsKey(t) &&
            knownMappings.get(t) instanceof Primary;
    }

    public boolean isKnownMocked(TraceObject t) {
        return knownMappings.containsKey(t) &&
            knownMappings.get(t) instanceof Mocked;
    }

    public ProgramObject traceToProgram(TraceObject t) {
        if (knownMappings.containsKey(t)) {
            return knownMappings.get(t);
        } else if (t instanceof Primitive) {
            // Primitives are both ProgramObjects and TraceObjects.
            return (Primitive) t;
        } else if (t instanceof Instance) {
            Instance i = (Instance) t;

            String className = Utils.classNameSlashesToPeriods(i.className);
            Mocked m = testMethodGenerator.addMock(className);

            knownMappings.put(t, m);
            return m;
        } else {
            throw new RuntimeException("Unexpected TraceObject: " + t);
        }
    }

    public void setProgramForTrace(TraceObject to, ProgramObject po) {
        assert to != null;
        assert po != null;
        assert !(to instanceof Primitive);
        assert !(po instanceof Primitive);

        if (knownMappings.containsKey(to)) {
            throw new RuntimeException("Already have ProgramObject for " +
                                       "TraceObject " + to + ": " +
                                       knownMappings.get(to) +
                                       ", but setting to: " + po);
        }

        knownMappings.put(to, po);
    }
}
