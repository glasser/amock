package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.TraceField;
import edu.mit.csail.pag.amock.util.MultiSet;

/**
 * A special implementation of Primary for classes implementing
 * "record types": those that we know how to properly initialize and
 * which are generally not stateful.
 */

public class RecordPrimary extends AbstractPrimary {
    public RecordPrimary(String classSourceName,
                         String varBaseName) {
        super(classSourceName, varBaseName);
    }

    // This method makes the primary have the given field value.  If
    // it's inconsistent with what it's been told before, maybe revert
    // to tweaking state?  Dunno.
    public void haveFieldValue(TraceField field,
                               ProgramObject value) {
        System.err.println("hFV: " + this + "; " + field + "; " + value);
    }

    protected List<ProgramObject> getConstructorArguments() {
        // NEXT:
        return Collections.emptyList();
    }
}
