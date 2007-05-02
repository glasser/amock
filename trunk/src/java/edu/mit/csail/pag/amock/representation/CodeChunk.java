package edu.mit.csail.pag.amock.representation;

import java.util.Collection;

/**
 * Interface representing a piece of code that can be printed.
 */
public interface CodeChunk {
    /**
     * Print the source of this chunk to the given LinePrinter.
     */
    public void printSource(LinePrinter a);

    /**
     * Returns a collection of the ProgramObjects used in this chunk.
     * The collection should not be modified, but the objects may be
     * mutated.
     */
    public Collection<ProgramObject> getProgramObjects();
}
