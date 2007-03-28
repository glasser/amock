package edu.mit.csail.pag.amock.representation;

/**
 * A series of chunks inside a jMock 2 expects() call.
 */

public class ExpectationsBlock extends IndentingEmptyLineSeparatedCodeBlock {
    private final String groupBuilderClass;

    public ExpectationsBlock(String groupBuilderClass) {
        this.groupBuilderClass = groupBuilderClass;
    }

    public void printSource(LinePrinter lp) {
        lp.line("checking(new " + groupBuilderClass + "() {{");
        super.printSource(lp);
        lp.line("}});");
    }
}
