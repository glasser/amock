package edu.mit.csail.pag.amock.processor;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class IdentifyIterators {
    private static final Set<TraceMethod> HARDCODED_TRACEMETHODS
        = new HashSet<TraceMethod>();
    static {
        HARDCODED_TRACEMETHODS.add(new TraceMethod("java/util/Enumeration",
                                                   "hasMoreElements",
                                                   "()Z"));
        HARDCODED_TRACEMETHODS.add(new TraceMethod("CH/ifa/draw/framework/FigureEnumeration",
                                                   "nextFigure",
                                                   "()LCH/ifa/draw/framework/Figure;"));
    }
    private static boolean HARDCODED_methodIsIteratorish(TraceMethod m) {
        return HARDCODED_TRACEMETHODS.contains(m);
    }

    public static boolean isPotentialIterator(InstanceInfo ii) {
//         if (! RecordPrimaryClassInfo.isRecordPrimaryClass(ii.instance.className)) {
//             return false;
//         }
//         RecordPrimaryClassInfo classInfo
//             = RecordPrimaryClassInfo.getClassInfo(ii.instance.className);

        boolean foundSomeIteratorishMethod = false;

        for (TraceMethod m : ii.invokedMethods) {
            if (m.isConstructor()) {
                continue;
            }
            
            if (HARDCODED_methodIsIteratorish(m)) {
                foundSomeIteratorishMethod = true;
            } else {
                return false;
            }
        }

        return foundSomeIteratorishMethod;
    }
}
