package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.Misc;

import java.util.*;

// Things to do when adding a new heuristic:
//  * in newProgramObjectForUnknownInstance, use some predicate to see
//    if the Instance should use the new type, and call a new method
//    on the ProgramObjectFactory if so
//  * implement the new ProgramObjectFactory/TestMethodGenerator method
//  * implement the new Primary type itself
//  * in Processor$TestedModeMain.processPreCall (and anywhere else relevant,
//    like field reads), branch to a new State to handle invocations
//    on the new type (like RecordPrimaryInvocation), and implement that
//    State

public class HeuristicBoundaryTranslator extends SingleObjectBoundaryTranslator {
    private final Map<Instance, InstanceInfo> instanceInformation;
    private final Hierarchy hierarchy;
    
    public HeuristicBoundaryTranslator(ProgramObjectFactory programObjectFactory,
                                       Map<Instance, InstanceInfo> instanceInformation,
                                       Hierarchy hierarchy) {
        super(programObjectFactory);
        this.instanceInformation = instanceInformation;
        this.hierarchy = hierarchy;
    }

    /**
     * This implementation makes special types of Primaries for "record type"
     * and Iterator pattern classes.
     */
    @Override protected ProgramObject newProgramObjectForUnknownInstance(Instance i,
                                                                         boolean isReturnValue) {
        InstanceInfo ii = instanceInformation.get(i);

        if (ii == null) {
            return super.newProgramObjectForUnknownInstance(i, isReturnValue);
        }

        if (IdentifyRecordPrimaries.isPotentialRecordPrimary(ii)) {
            return getProgramObjectFactory().addRecordPrimary(i.className);
        }

        if (IdentifyIterators.isPotentialIterator(ii, hierarchy)) {
            return getProgramObjectFactory().addIterationPrimary(i.className);
        }

        return super.newProgramObjectForUnknownInstance(i, isReturnValue);
    }
}
