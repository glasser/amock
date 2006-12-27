package edu.mit.csail.pag.amock.representation;

import java.io.IOException;

public interface CodeChunk {
    /**
     * Print the source of this chunk to the given LinePrinter.
     */
    public void printSource(LinePrinter a) throws IOException;
}
