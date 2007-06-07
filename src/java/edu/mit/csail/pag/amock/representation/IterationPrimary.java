package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.MultiSet;
import edu.mit.csail.pag.amock.hooks.IterationPrimaryClassInfo;

/**
 * A special implementation of Primary for classes implementing
 * the iterator pattern.
 */
public class IterationPrimary extends AbstractPrimary {
    private final List<ProgramObject> iteratedValues
        = new ArrayList<ProgramObject>();
    
    private final IterationPrimaryClassInfo classInfo;

    public IterationPrimary(String className,
                            String implementingClassSourceName,
                            String varBaseName,
                            Hierarchy hierarchy) {
        super(implementingClassSourceName, varBaseName);

        assert IterationPrimaryClassInfo.isIterationPrimaryClass(className,
                                                                 hierarchy);
        classInfo = IterationPrimaryClassInfo.getClassInfo(className,
                                                           hierarchy);
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

    public void returnsFromMethod(TraceMethod m,
                                  ProgramObject po) {
        if (classInfo.methodGetsNextItem(m)) {
            iteratedValues.add(po);
        }
    }
}
