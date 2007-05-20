package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.TraceField;
import edu.mit.csail.pag.amock.trace.Primitive;
import edu.mit.csail.pag.amock.util.MultiSet;

/**
 * A special implementation of Primary for classes implementing
 * "record types": those that we know how to properly initialize and
 * which are generally not stateful.
 */

public class RecordPrimary extends AbstractPrimary {
    private final Map<TraceField, ProgramObject> fieldValues
        = new HashMap<TraceField, ProgramObject>();
    
    public RecordPrimary(String classSourceName,
                         String varBaseName) {
        super(classSourceName, varBaseName);
    }

    // This method makes the primary have the given field value.  If
    // it's inconsistent with what it's been told before, maybe revert
    // to tweaking state?  Dunno.
    public void haveFieldValue(TraceField field,
                               ProgramObject value) {
        if (fieldValues.containsKey(field)) {
            // XXX this is wrong: should be OK if it's the same!
            System.err.println("duplicate hFV: " + this + "; " + field + "; " + value);
        } else {
            fieldValues.put(field, value);
        }
    }

    private static final TraceField F_X
        = new TraceField("java/awt/Rectangle", "x", "I");
    private static final TraceField F_Y
        = new TraceField("java/awt/Rectangle", "y", "I");
    private static final TraceField F_WIDTH
        = new TraceField("java/awt/Rectangle", "width", "I");
    private static final TraceField F_HEIGHT
        = new TraceField("java/awt/Rectangle", "height", "I");

    protected List<ProgramObject> getConstructorArguments() {
        List<ProgramObject> args = new ArrayList<ProgramObject>();

        for (TraceField f : new TraceField[] { F_X, F_Y, F_WIDTH, F_HEIGHT }) {
            if (fieldValues.containsKey(f)) {
                args.add(fieldValues.get(f));
            } else {
                args.add(new Primitive(0));
            }
        }

        return args;
    }
}
