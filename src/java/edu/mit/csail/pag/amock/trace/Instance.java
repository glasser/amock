package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

public class Instance extends TraceObject implements Serializable {
    public final String className;
    public final int id;

    public Instance(String className, int id) {
        this.className = className;
        this.id = id;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Instance)) {
            return false;
        }
        Instance i = (Instance) o;

        return className.equals(i.className) &&
            id == i.id;
    }

    @Override public int hashCode() {
        return className.hashCode() + 37*id;
    }
}
