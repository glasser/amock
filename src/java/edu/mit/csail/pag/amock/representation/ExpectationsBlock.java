package edu.mit.csail.pag.amock.representation;

/**
 * A series of chunks inside a jMock 2 expects() call.
 */

public class ExpectationsBlock extends IndentingEmptyLineSeparatedCodeBlock {
    private final String groupBuilderClass;
    private boolean empty = true;

    public ExpectationsBlock(String groupBuilderClass) {
        this.groupBuilderClass = groupBuilderClass;
    }

    public void printSource(LinePrinter lp) {
        if (empty) {
            lp.line("// [No expectations.]");
        } else {
            lp.line("verifyThenCheck(new " + groupBuilderClass + "() {{");
            super.printSource(lp);
            lp.line("}});");
        }
    }

    @Override public void addChunk(CodeChunk c) {
        empty = false;
        super.addChunk(c);
    }
}
