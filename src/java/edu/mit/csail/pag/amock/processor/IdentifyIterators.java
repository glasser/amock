package edu.mit.csail.pag.amock.processor;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.hooks.IterationPrimaryClassInfo;

public class IdentifyIterators {
    public static boolean isPotentialIterator(InstanceInfo ii,
                                              Hierarchy hierarchy) {
        if (! IterationPrimaryClassInfo.isIterationPrimaryClass(ii.instance.className,
                                                                hierarchy)) {
            return false;
        }

        IterationPrimaryClassInfo classInfo
            = IterationPrimaryClassInfo.getClassInfo(ii.instance.className,
                                                     hierarchy);

        for (TraceMethod m : ii.invokedMethods) {
            if (! classInfo.methodIsBenign(m)) {
                return false;
            }
        }

        return true;
    }
}
