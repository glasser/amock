package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.MultiSet;

/**
 * A special implementation of Primary for classes implementing
 * "record types": those that we know how to properly initialize and
 * which are generally not stateful.
 */

// BRAINSTORM
//
// the right idea is basically a map from field or method to
// constructor slot.
// also need to default for the others.

public class RecordPrimary extends AbstractPrimary {
    private final List<ProgramObject> fieldValues
        = new ArrayList<ProgramObject>();

    // vs rectangle
    private final boolean isMouseEvent;

    public RecordPrimary(String classSourceName,
                         String varBaseName) {
        super(classSourceName, varBaseName);
        // note: source name isn't actually enough information!!! loses package
        if (classSourceName.equals("MouseEvent")) {
            isMouseEvent = true;
        } else {
            assert classSourceName.equals("Rectangle");
            isMouseEvent = false;
        }

        if (isMouseEvent) {
            fieldValues.add(new Primitive(null));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(false));
        } else {
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
            fieldValues.add(new Primitive(0));
        }

    }

    // This method makes the primary have the given field value.  If
    // it's inconsistent with what it's been told before, maybe revert
    // to tweaking state?  Dunno.
    public void haveFieldValue(TraceField field,
                               ProgramObject value) {
        assert !isMouseEvent;
        if (!RECTANGLE_FIELDS.containsKey(field)) {
            System.err.println("unknown field!: " + field);
            return;
        }
        
        int index = RECTANGLE_FIELDS.get(field);
//         if (fieldValues.get(field)) {
//             // XXX this is wrong: should be OK if it's the same!
//             System.err.println("duplicate hFV: " + this + "; " + field + "; " + value);
//         } else {
        fieldValues.set(index, value);
    }

    // This method makes the primary as if it had returned from the given
    // method.  If it's inconsistent with what it's been told before,
    // maybe revert to tweaking state?  Dunno.
    public void returnsFromMethod(TraceMethod method,
                                  ProgramObject value) {
        assert isMouseEvent;
        if (!MOUSEEVENT_METHODS.containsKey(method)) {
            System.err.println("unknown method!: " + method);
            return;
        }
        
        int index = MOUSEEVENT_METHODS.get(method);
//         if (fieldValues.get(field)) {
//             // XXX this is wrong: should be OK if it's the same!
//             System.err.println("duplicate hFV: " + this + "; " + field + "; " + value);
//         } else {
        fieldValues.set(index, value);
    }

    private static final Map<TraceField, Integer> RECTANGLE_FIELDS
        = new HashMap<TraceField, Integer>();
    private static final Map<TraceMethod, Integer> MOUSEEVENT_METHODS
        = new HashMap<TraceMethod, Integer>();

    static {
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "x", "I"), 0);
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "y", "I"), 1);
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "width", "I"), 2);
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "height", "I"), 3);

        MOUSEEVENT_METHODS.put(new TraceMethod("java/awt/event/MouseEvent",
                                               "getX", "()I"), 4);
        MOUSEEVENT_METHODS.put(new TraceMethod("java/awt/event/MouseEvent",
                                               "getY", "()I"), 5);
    }

    protected List<ProgramObject> getConstructorArguments() {
        return Collections.unmodifiableList(fieldValues);
    }
}
