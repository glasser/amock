package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import edu.mit.csail.pag.amock.hooks.RecordPrimaryClassInfo;

/**
 * A special implementation of Primary for classes implementing
 * "record types": those that we know how to properly initialize and
 * which are generally not stateful.
 */
public class RecordPrimary extends AbstractPrimary {
    
    private final List<ProgramObject> argValues
        = new ArrayList<ProgramObject>();
    private final List<Boolean> argInitialized
        = new ArrayList<Boolean>();

    private final RecordPrimaryClassInfo classInfo;

    public RecordPrimary(ClassName className) {
        super(className);

        assert RecordPrimaryClassInfo.isRecordPrimaryClass(className);
        classInfo = RecordPrimaryClassInfo.getClassInfo(className);

        for (ProgramObject po : classInfo.slotDefaults) {
            // XXX: should I do some sort of clone here?
            argValues.add(po);
            argInitialized.add(false);
        }
    }

    @Override public void resolveNames(ClassNameResolver cr,
                                       VariableNameBaseResolver vr) {
        super.resolveNames(cr, vr);
        for (ProgramObject po : this.argValues) {
            po.resolveNames(cr, vr);
        }
    }

    private boolean needsDeclaration = true;

    public boolean needsDeclaration() {
        return needsDeclaration;
    }

    public void doesNotNeedDeclaration() {
        needsDeclaration = false;
    }

    // This method makes the primary have the given field value.  If
    // it's inconsistent with what it's been told before, maybe revert
    // to tweaking state?  Dunno.
    public void haveFieldValue(TraceField field,
                               ProgramObject value) {
        if (!classInfo.fieldSlots.containsKey(field)) {
            System.err.println("unknown field!: " + field);
            return;
        }
        
        int index = classInfo.fieldSlots.get(field);
        if (!argInitialized.get(index)) {
            argValues.set(index, value);
            argInitialized.set(index, true);
        }
    }

    // This method makes the primary as if it had returned from the given
    // method.  If it's inconsistent with what it's been told before,
    // maybe revert to tweaking state?  Dunno.
    public void returnsFromMethod(TraceMethod method,
                                  ProgramObject value) {
        if (!classInfo.methodSlots.containsKey(method)) {
            System.err.println("unknown method!: " + method);
            return;
        }
        
        int index = classInfo.methodSlots.get(method);
        if (!argInitialized.get(index)) {
            argValues.set(index, value);
            argInitialized.set(index, true);
        }
    }

    protected List<ProgramObject> getConstructorArguments() {
        return Collections.unmodifiableList(argValues);
    }
}
