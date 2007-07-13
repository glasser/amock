package edu.mit.csail.pag.amock.trace;

import java.util.*;
import java.io.Serializable;
import java.lang.reflect.Modifier;

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

    public Collection<ClassName> allSupers() {
        Collection<ClassName> all = new ArrayList<ClassName>();
        all.add(superName);
        all.addAll(Arrays.asList(interfaces));
        return all;
    }

    // Only call this on code (JDK, etc) that you're comfortable
    // loading at processor-time!
    public static HierarchyEntry createWithReflection(ClassName name) {
        Class<?> cls;
        try {
            cls = Class.forName(name.asClassForNameArgument());
        } catch (ClassNotFoundException e) {
            // Shouldn't happen: we've observed this class in the
            // trace or something.
            throw new RuntimeException(e);
        }

        Class<?> superCls = cls.getSuperclass();
        ClassName superName
            = ClassName.fromDotted( superCls == null
                                    ? "java.lang.Object"
                                    : superCls.getName() );

        Class[] ifClasses = cls.getInterfaces();
        ClassName[] ifNames = new ClassName[ifClasses.length];

        for (int i = 0; i < ifClasses.length; i++) {
            ifNames[i] = ClassName.fromDotted(ifClasses[i].getName());
        }

        return new HierarchyEntry(name, superName, ifNames,
                                  Modifier.isPublic(cls.getModifiers()));
    }
}
