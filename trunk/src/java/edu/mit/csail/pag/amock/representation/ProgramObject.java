package edu.mit.csail.pag.amock.representation;

/**
 * Represents something that can be manipulated in the generated test;
 * for example, a primary object, mock, or primitive.  Anything that
 * can be passed as an argument to something should implement this.
 */
public interface ProgramObject {
    /**
     * Returns a source representation of the object.  Note that you
     * should avoid calling this while building up a test
     * representation, and rather should only call it during a
     * printSource, because it's possible that it might change as more
     * information about the object is learned.  (For example, whether
     * a mock needs to be saved into a local variable or whether it
     * can just be a throwaway dummy.)
     */
    public String getSourceRepresentation();
    /**
     * Used to track the number of times a ProgramObject will apear in
     * the program's code.
     */
    public void incrementReferenceCount();
}
