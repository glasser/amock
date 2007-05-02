package edu.mit.csail.pag.amock.representation;

import java.util.*;

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

    public Collection<ProgramObject> getProgramObjects() {
        return primary.getProgramObjects();
    }
}
