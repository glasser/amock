package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import org.objectweb.asm.Type;

/**
 * A special implementation of Primary for...
 */
public class StaticFieldPrimary implements Primary {
    private final ClassName className;
    private String classSourceName = null;
    
    public StaticFieldPrimary(ClassName className) {
        this.className = className;
    }

    public String getClassSourceName() {
        assert classSourceName != null;
        return classSourceName;
    }

    public boolean needsDeclaration() {
        return false;
    }

    public void doesNotNeedDeclaration() {
        // Do nothing.
    }

    // TODO should not be in interface
    public String getPrimaryVariableName() {
        assert false;
        return "";
    }

    // TODO should not be in interface
    public String getConstructor() {
        assert false;
        return "";
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(this);
        return pos;
    }

    public String getSourceRepresentation() {
        return getClassSourceName() + ".INSTANCE";
    }
    public void usedAsType(Type t) {
        // XXX: could do some checking here of the hierarchy
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.classSourceName == null) {
            this.classSourceName = cr.getSourceName(this.className);
        }
    }

}
