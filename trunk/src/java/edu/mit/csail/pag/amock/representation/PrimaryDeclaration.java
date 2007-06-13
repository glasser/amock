package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class PrimaryDeclaration implements CodeChunk {
    private final DeclarablePrimary primary;

    public PrimaryDeclaration(DeclarablePrimary primary) {
        this.primary = primary;
    }

    public void printSource(LinePrinter p) {
        if (primary.needsDeclaration()) {
            p.line("final " + primary.getClassSourceName() + " "
                   + primary.getPrimaryVariableName()
                   + " = " + primary.getConstructor() + ";");
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        return primary.getProgramObjects();
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        primary.resolveNames(cr, vr);
    }
}
