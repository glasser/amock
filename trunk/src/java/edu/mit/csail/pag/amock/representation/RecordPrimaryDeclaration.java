package edu.mit.csail.pag.amock.representation;

public class RecordPrimaryDeclaration extends PrimaryDeclaration
    implements SimpleDeclaration /* wishful thinking */ {
    public RecordPrimaryDeclaration(DeclarablePrimary p) {
        super(p);
    }
    
    public String getSortKey() {
        if (getPrimary().needsDeclaration()) {
            return "RECORD " + getPrimary().getPrimaryVariableName();
        } else {
            return "doesn't matter";
        }
    }


}
