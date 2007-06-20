package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.util.ClassName;
import org.objectweb.asm.Type;

public class StaticTarget implements ExpectationTarget {
    private ClassName className;

    private String resolvedClassName;
    
    public StaticTarget(ClassName className) {
        setClassName(className);
    }

    public void setClassName(ClassName className) {
        this.className = className;
        this.resolvedClassName = null;
    }

    public String getExpectationTargetName() {
        return resolvedClassName + ".class";
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.resolvedClassName == null) {
            this.resolvedClassName = cr.getSourceName(this.className);
        }
    }

    public void usedAsType(Type t) {
        assert t.getClassName().equals(className.dotted());
    }

    public String getSourceRepresentation() {
        return getExpectationTargetName();
    }
}
