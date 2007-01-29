package edu.mit.csail.pag.amock.trace;

import edu.mit.csail.pag.amock.representation.ProgramObject;

public class Primitive extends TraceObject implements ProgramObject {
    public final Object value;

    public Primitive(Object value) {
        this.value = value;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Primitive)) {
            return false;
        }
        Primitive p = (Primitive) o;

        return value.equals(p.value);
    }

    @Override public int hashCode() {
        return value.hashCode() * 3;
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        return null;
    }
}
