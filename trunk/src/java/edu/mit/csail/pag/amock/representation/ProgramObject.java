package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;

/**
 * Represents something that can be manipulated in the generated test;
 * for example, a primary object, mock, or primitive.  Anything that
 * can be passed as an argument to something should implement this.
 */
public interface ProgramObject {
    public String getSourceRepresentation();

    /**
     * Inform the ProgramObject that it must be capable of being used
     * in a context where an object of the given type is expected (in
     * the generated test).  (Can be used to calculate the most
     * general possible type for a mock, etc.)
     */
    public void usedAsType(Type t);
}
