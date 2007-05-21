package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.Misc;

import java.util.*;

public class SingleObjectBoundaryTranslator implements BoundaryTranslator {
    private final ProgramObjectFactory programObjectFactory;
    private final Map<TraceObject, ProgramObject> knownMappings =
        new HashMap<TraceObject, ProgramObject>();

    public SingleObjectBoundaryTranslator(ProgramObjectFactory programObjectFactory) {
        this.programObjectFactory = programObjectFactory;
    }

    public boolean isKnownPrimary(TraceObject t) {
        return knownMappings.containsKey(t) &&
            knownMappings.get(t) instanceof Primary;
    }

    public boolean isKnownMocked(TraceObject t) {
        return knownMappings.containsKey(t) &&
            knownMappings.get(t) instanceof Mocked;
    }

    /**
     * Returns a ProgramObject corresponding to the given TraceObject.
     * If the argument is an Instance which has not already been
     * mapped to a ProgramObject, it uses
     * newProgramObjectForUnknownInstance (which can be overridden) to
     * get the corresponding ProgramObject.
     */
    public ProgramObject traceToProgram(TraceObject t) {
        if (knownMappings.containsKey(t)) {
            return knownMappings.get(t);
        } else if (t instanceof Primitive) {
            // Primitives are both ProgramObjects and TraceObjects.
            return (Primitive) t;
        } else if (t instanceof Instance) {
            Instance i = (Instance) t;

            ProgramObject po = newProgramObjectForUnknownInstance(i);

            setProgramForTrace(t, po);
            return po;
        } else {
            throw new RuntimeException("Unexpected TraceObject: " + t);
        }
    }

    /**
     * This implementation makes a Mocked for any unknown Instance.  A
     * subclass may override this behavior.
     */
    protected ProgramObject newProgramObjectForUnknownInstance(Instance i) {
        String className = Misc.classNameSlashesToPeriods(i.className);
        return getProgramObjectFactory().addMock(className);
    }

    /**
     * Returns a ProgramObjectFactory being used by the translator; may
     * be useful for subclasses implementing
     * newProgramObjectForUnknownInstance.
     */
    protected ProgramObjectFactory getProgramObjectFactory() {
        return programObjectFactory;
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
