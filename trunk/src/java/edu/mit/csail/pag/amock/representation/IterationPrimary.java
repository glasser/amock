package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.MultiSet;

/**
 * A special implementation of Primary for classes implementing
 * the iterator pattern.
 */
public class IterationPrimary extends AbstractPrimary {
    private final List<ProgramObject> iteratedValues
        = new ArrayList<ProgramObject>();

    public IterationPrimary(String className,
                            String classSourceName,
                            String varBaseName) {
        super(classSourceName, varBaseName);

//         assert RecordPrimaryClassInfo.isRecordPrimaryClass(className);
//         classInfo = RecordPrimaryClassInfo.getClassInfo(className);

//         for (ProgramObject po : classInfo.slotDefaults) {
//             // XXX: should I do some sort of clone here?
//             argValues.add(po);
//             argInitialized.add(false);
//         }
    }

    private boolean needsDeclaration = true;

    public boolean needsDeclaration() {
        return needsDeclaration;
    }

    public void doesNotNeedDeclaration() {
        needsDeclaration = false;
    }

    @Override protected List<ProgramObject> getConstructorArguments() {
        return Collections.unmodifiableList(iteratedValues);
    }

    public void returnsFromNext(ProgramObject po) {
        iteratedValues.add(po);
    }
}
