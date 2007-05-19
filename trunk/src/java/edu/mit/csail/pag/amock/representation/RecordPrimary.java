package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

/**
 * A special implementation of Primary for classes implementing
 * "record types": those that we know how to properly initialize and
 * which are generally not stateful.
 */

public class RecordPrimary implements Primary {
    private final String classSourceName;
    private final String varBaseName;

    public RecordPrimary(String classSourceName,
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

    public String getConstructor() {
        StringBuilder s = new StringBuilder();
        s.append("new ");
        s.append(getClassSourceName());
        s.append("(");

        boolean first = true;

        // NEXT:
//         for (ProgramObject o : constructorArguments) {
//             if (first) {
//                 first = false;
//             } else {
//                 s.append(", ");
//             }

//             s.append(o.getSourceRepresentation());
//         }

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
        return pos;
    }
}
