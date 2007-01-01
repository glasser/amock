package edu.mit.csail.pag.amock.representation;

import java.util.*;

public abstract class AbstractCodeBlock implements CodeBlock {
    private final List<CodeChunk> chunks
        = new ArrayList<CodeChunk>();

    public void addChunk(CodeChunk c) {
        chunks.add(c);
    }

    /**
     * Should be called by subclass in its printSource to print out
     * the nested chunks.
     */
    protected void printChunks(LinePrinter lp) {
        for (CodeChunk c : chunks) {
            c.printSource(lp);
        }
    }

    abstract public void printSource(LinePrinter lp);
}
