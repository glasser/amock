package edu.mit.csail.pag.amock.processor;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;
import edu.mit.csail.pag.amock.hooks.IterationPrimaryClassInfo;

public class IdentifySafeStaticFields {
    public static TraceField cameFromSafeStaticField(InstanceInfo ii) {
        for (TraceField field : ii.staticFields) {
            // HARDCODE
            if (field.name.equals("INSTANCE") || field.declaringClass.equals(ClassName.fromDotted("org.tmatesoft.svn.cli.SVNArgument"))) {
                return field;
            }
        }

        return null;
    }
}
//         if (! IterationPrimaryClassInfo.isIterationPrimaryClass(ii.instance.className,
//                                                                 hierarchy)) {
//             return false;
//         }

//         IterationPrimaryClassInfo classInfo
//             = IterationPrimaryClassInfo.getClassInfo(ii.instance.className,
//                                                      hierarchy);

//         for (TraceMethod m : ii.invokedMethods) {
//             if (! classInfo.methodIsBenign(m)) {
//                 return false;
//             }
//         }

//         return true;
