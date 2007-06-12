package edu.mit.csail.pag.amock.trace;

import java.io.Serializable;

import edu.mit.csail.pag.amock.util.ClassName;

public class HierarchyEntry implements Serializable {
    public final ClassName className;
    public final ClassName superName;
    public final ClassName[] interfaces;
    public final boolean isPublic;

    public HierarchyEntry(ClassName className,
                          ClassName superName,
                          ClassName[] interfaces,
                          boolean isPublic) {
        this.className = className;
        this.superName = superName;
        this.interfaces = interfaces;
        this.isPublic = isPublic;
    }

    public static HierarchyEntry fromSlashed(String c,
                                             String s,
                                             String[] ifs,
                                             boolean isPublic) {
        ClassName[] cifs = new ClassName[ifs.length];
        for (int i = 0; i < ifs.length; i++) {
            cifs[i] = ClassName.fromSlashed(ifs[i]);
        }

        return new HierarchyEntry(ClassName.fromSlashed(c),
                                  ClassName.fromSlashed(s),
                                  cifs,
                                  isPublic);
    }
}
