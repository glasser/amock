package edu.mit.csail.pag.amock.representation;

public class ExpectationDeclaration implements CodeChunk {
    private final Expectation expectation;

    public ExpectationDeclaration(Expectation expectation) {
        this.expectation = expectation;
    }

    public void printSource(LinePrinter p) {
    }
}
