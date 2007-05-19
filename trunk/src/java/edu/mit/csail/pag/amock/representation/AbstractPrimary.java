package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.TraceField;
import edu.mit.csail.pag.amock.util.MultiSet;

public abstract class AbstractPrimary implements Primary {
    private final String classSourceName;
    private final String varBaseName;

    public AbstractPrimary(String classSourceName,
                           String varBaseName) {
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getPrimaryVariableName() {
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
        return getPrimaryVariableName();
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(this);
        pos.addAll(getConstructorArguments());
        return pos;
    }
}
