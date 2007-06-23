package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import org.objectweb.asm.Type;

/**
 * A special implementation of Primary for...
 */
public class StaticFieldPrimary extends AbstractProgramObject
    implements Primary {
    private final TraceField field;
    private String classSourceName = null;
    
    public StaticFieldPrimary(TraceField field) {
        this.field = field;
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(this);
        return pos;
    }

    public String getPrimaryExecutionReceiverRepresentation() {
        return getSourceRepresentation();
    }

    public String getSourceRepresentation() {
        return classSourceName + "." + field.name;
    }
    public void usedAsType(Type t) {
        // XXX: could do some checking here of the hierarchy
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.classSourceName == null) {
            this.classSourceName = cr.getSourceName(this.field.declaringClass);
        }
    }

}
