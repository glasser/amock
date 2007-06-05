package edu.mit.csail.pag.amock.processor;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.hooks.IterationPrimaryClassInfo;

public class IdentifyIterators {
    public static boolean isPotentialIterator(InstanceInfo ii) {
        if (! IterationPrimaryClassInfo.isIterationPrimaryClass(ii.instance.className)) {
            return false;
        }
        IterationPrimaryClassInfo classInfo
            = IterationPrimaryClassInfo.getClassInfo(ii.instance.className);

        for (TraceMethod m : ii.invokedMethods) {
            if (! classInfo.methodIsBenign(m)) {
                return false;
            }
        }

        return true;
    }
}
