package edu.mit.csail.pag.amock.representation;

/**
 * Interface representing a piece of code that can be printed.
 */
public interface CodeChunk {
    /**
     * Print the source of this chunk to the given LinePrinter.
     */
    public void printSource(LinePrinter a);
}
