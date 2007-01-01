package edu.mit.csail.pag.amock.representation;

/**
 * Interface representing a piece of code that can contain other
 * CodeChunks.
 */

public interface CodeBlock extends CodeChunk {
    /**
     * Append the given CodeChunk to this CodeBlock.
     */
    public void addChunk(CodeChunk c);
}
