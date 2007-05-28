package edu.mit.csail.pag.amock.representation;

public interface OptionallyDeclarable extends ProgramObject {
    public boolean needsDeclaration();
    public void doesNotNeedDeclaration();
}
