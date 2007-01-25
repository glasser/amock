package edu.mit.csail.pag.amock.trace;

public class Primitive extends TraceObject {
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
}
