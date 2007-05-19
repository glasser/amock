package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class ExplicitlyDeclaredPrimary implements Primary {
    private final String classSourceName;
    private final String varBaseName;
    private final ProgramObject[] constructorArguments;

    public ExplicitlyDeclaredPrimary(String classSourceName,
                                     String varBaseName,
                                     ProgramObject[] constructorArguments) {
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
        this.constructorArguments = constructorArguments;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getPrimaryVariableName() {
        return "tested" + varBaseName;
    }

    public String getConstructor() {
        StringBuilder s = new StringBuilder();
        s.append("new ");
        s.append(getClassSourceName());
        s.append("(");

        boolean first = true;

        for (ProgramObject o : constructorArguments) {
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
        pos.addAll(Arrays.asList(constructorArguments));
        pos.add(this);
        return pos;
    }
}
