package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.Misc;

import java.util.*;

public class RecordBoundaryTranslator extends SingleObjectBoundaryTranslator {
    private final Set<Instance> potentialRecordPrimaries;
    
    public RecordBoundaryTranslator(ProgramObjectFactory programObjectFactory,
                                    Set<Instance> potentialRecordPrimaries) {
        super(programObjectFactory);
        this.potentialRecordPrimaries = potentialRecordPrimaries;
    }

    /**
     * This implementation makes a Primary for certain "record type" classes.
     */
    @Override
    protected ProgramObject newProgramObjectForUnknownInstance(Instance i) {
        if (! potentialRecordPrimaries.contains(i)) {
            return super.newProgramObjectForUnknownInstance(i);
        }

        String className = Misc.classNameSlashesToPeriods(i.className);
        
        return getProgramObjectFactory().addRecordPrimary(className,
                                                          true);
    }
}
