package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class BasicCodeBlock implements CodeBlock {
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
        boolean first = true;
        for (CodeChunk c : chunks) {
            if (first) {
                first = false;
            } else {
                betweenChunks(lp);
            }

            c.printSource(lp);
        }
    }

    /*
     * Subclasses can override this to do something special in between
     * chunks.
     */
    protected void betweenChunks(LinePrinter lp) {
        // Do nothing, by default.
    }

    /*
     * Subclasses should override this to do something more
     * interesting than just printing the chunks.
     */
    public void printSource(LinePrinter lp) {
        printChunks(lp);
    }
}
