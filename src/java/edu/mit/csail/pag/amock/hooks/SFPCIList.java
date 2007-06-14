package edu.mit.csail.pag.amock.hooks;

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

public class SFPCIList extends StaticFieldPrimaryClassInfo {
    private final Set<String> fields
        = new HashSet<String>();

    public SFPCIList(ClassName className) {
        super(className);
    }

    public boolean isSafeStaticField(TraceField f) {
        return fields.contains(f.name);
    }

    public void reflectivelyFillFields(Class<?> c) {
        for (Field f : c.getFields()) {
            // only static fields.
            if ((f.getModifiers() & Modifier.STATIC) == 0) {
                continue;
            }
            
            // Let's assume we only care about final fields.
            if ((f.getModifiers() & Modifier.FINAL) == 0) {
                continue;
            }

            fields.add(f.getName());
        }
    }
}
