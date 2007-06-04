package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.hooks.RecordPrimaryClassInfo;

public class IdentifyRecordPrimaries {
    public static boolean isPotentialRecordPrimary(InstanceInfo ii) {
        if (! RecordPrimaryClassInfo.isRecordPrimaryClass(ii.instance.className)) {
            return false;
        }
        RecordPrimaryClassInfo classInfo
            = RecordPrimaryClassInfo.getClassInfo(ii.instance.className);

        for (TraceMethod m : ii.invokedMethods) {
            if (! classInfo.methodIsBenign(m)) {
                return false;
            }
        }

        return true;
    }
}
