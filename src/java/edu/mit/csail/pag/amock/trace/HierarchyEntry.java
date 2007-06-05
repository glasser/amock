package edu.mit.csail.pag.amock.trace;

public class HierarchyEntry {
    // all fields have slashes
    public final String className;
    public final String superName;
    public final String[] interfaces;

    public HierarchyEntry(String className,
                          String superName,
                          String[] interfaces) {
        this.className = className;
        this.superName = superName;
        this.interfaces = interfaces;
    }
}
