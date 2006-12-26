package edu.mit.csail.pag.amock.representation;

public interface CodeChunk {
    /**
     * Print the source of this chunk to the given Appendable.
     */
    public void printSource(Appendable a);
}
