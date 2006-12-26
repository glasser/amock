package edu.mit.csail.pag.amock.representation;

public interface TestMethodGenerator {
    /**
     * Print the source of the generated method to the given Appendable.
     */
    public void printSource(Appendable a);
}