package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class PrimaryDeclaration implements CodeChunk {
    private final Primary primary;

    public PrimaryDeclaration(Primary primary) {
        this.primary = primary;
    }

    public void printSource(LinePrinter p) {
        p.line(primary.getClassSourceName() + " "
               + primary.getPrimaryVariableName()
               + " = " + primary.getConstructor() + ";");
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        return primary.getProgramObjects();
    }
}
