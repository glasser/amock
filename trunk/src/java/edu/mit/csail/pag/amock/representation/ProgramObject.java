package edu.mit.csail.pag.amock.representation;

/**
 * Represents something that can be manipulated in the generated test;
 * for example, a primary object, mock, or primitive.  Anything that
 * can be passed as an argument to something should implement this.
 */
public interface ProgramObject {
    public String getSourceRepresentation();
    /**
     * Used to track the number of times a ProgramObject will apear in
     * the program's code.
     */
    public void incrementReferenceCount();
}
