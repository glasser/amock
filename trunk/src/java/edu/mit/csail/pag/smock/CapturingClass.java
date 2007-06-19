package edu.mit.csail.pag.smock;

public class CapturingClass {
    public final Class<?> cls;

    public CapturingClass(Class<?> cls) {
        this.cls = cls;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof CapturingClass)) {
            return false;
        }
        CapturingClass c = (CapturingClass) o;

        return this.cls.equals(c.cls);
    }

    @Override public int hashCode() {
        return this.cls.hashCode() + 1;
    }
}
