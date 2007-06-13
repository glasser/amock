package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;
import java.util.*;
import edu.mit.csail.pag.amock.trace.TraceField;
import edu.mit.csail.pag.amock.util.*;

public abstract class AbstractPrimary implements Primary {
    private final ClassName fullClassName;
    
    private String classSourceName = null;
    private String varBaseName = null;

    public AbstractPrimary(ClassName fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getClassSourceName() {
        assert classSourceName != null;
        return classSourceName;
    }

    public String getPrimaryVariableName() {
        assert varBaseName != null;
        return "tested" + varBaseName;
    }

    protected abstract List<ProgramObject> getConstructorArguments();
    
    public String getConstructor() {
        StringBuilder s = new StringBuilder();
        s.append("new ");
        s.append(getClassSourceName());
        s.append("(");

        boolean first = true;

        for (ProgramObject o : getConstructorArguments()) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            s.append(o.getSourceRepresentation());
        }

        s.append(")");
        
        return s.toString();
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        if (needsDeclaration()) {
            return getPrimaryVariableName();
        } else {
            return getConstructor();
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(this);
        pos.addAll(getConstructorArguments());
        return pos;
    }

    public void usedAsType(Type t) {
        // XXX: could do some checking here of the hierarchy
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.classSourceName == null) {
            this.classSourceName = cr.getSourceName(this.fullClassName);
            if (needsDeclaration()) {
                this.varBaseName = vr.getVarNameBase(this.fullClassName);
            }
        }

        for (ProgramObject po : getConstructorArguments()) {
            po.resolveNames(cr, vr);
        }
    }

}
