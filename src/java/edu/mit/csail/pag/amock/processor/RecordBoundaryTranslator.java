package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.Misc;

import java.util.*;

public class RecordBoundaryTranslator extends SingleObjectBoundaryTranslator {
    private final Map<Instance, InstanceInfo> instanceInformation;
    
    public RecordBoundaryTranslator(ProgramObjectFactory programObjectFactory,
                                    Map<Instance, InstanceInfo> instanceInformation) {
        super(programObjectFactory);
        this.instanceInformation = instanceInformation;
    }

    /**
     * This implementation makes a Primary for certain "record type" classes.
     */
    @Override
    protected ProgramObject newProgramObjectForUnknownInstance(Instance i) {
        InstanceInfo ii = instanceInformation.get(i);

        if (ii == null ||
            !IdentifyRecordPrimaries.isPotentialRecordPrimary(ii)) {
            return super.newProgramObjectForUnknownInstance(i);
        }

        return getProgramObjectFactory().addRecordPrimary(i.className,
                                                          true);
    }
}
