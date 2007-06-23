package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;
import edu.mit.csail.pag.amock.util.ClassName;

public class Instance implements TraceObject, Serializable {
    public final ClassName className;
    public final int id;

    public Instance(ClassName className, int id) {
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

    @Override public String toString() {
        return "[" + className.dotted() + ":" + id + "]";
    }
}
