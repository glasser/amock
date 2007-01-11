package edu.mit.csail.pag.amock.representation;

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
}
