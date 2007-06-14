package edu.mit.csail.pag.amock.processor;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;
import edu.mit.csail.pag.amock.hooks.StaticFieldPrimaryClassInfo;

public class IdentifySafeStaticFields {
    public static TraceField cameFromSafeStaticField(InstanceInfo ii) {
        for (TraceField field : ii.staticFields) {
            if (isSafeStaticField(field)) {
                return field;
            }
        }

        return null;
    }

    private static boolean isSafeStaticField(TraceField field) {
        if (! StaticFieldPrimaryClassInfo.isStaticFieldPrimaryClass(field.declaringClass)) {
            return false;
        }

        StaticFieldPrimaryClassInfo classInfo
            = StaticFieldPrimaryClassInfo.getClassInfo(field.declaringClass);

        return classInfo.isSafeStaticField(field);
    }
}
