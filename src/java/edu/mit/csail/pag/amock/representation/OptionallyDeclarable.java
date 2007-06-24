package edu.mit.csail.pag.amock.representation;

public interface OptionallyDeclarable extends ProgramObject {
    public boolean needsDeclaration();
    public void doesNotNeedDeclaration();
    /** The maximum number of times that the object can show up in the
     * TestMethodGenerator and still be undeclared.
     */
    public int maxUsesForUndeclared();
}
