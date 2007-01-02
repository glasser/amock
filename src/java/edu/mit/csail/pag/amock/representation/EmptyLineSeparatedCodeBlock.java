package edu.mit.csail.pag.amock.representation;

public class EmptyLineSeparatedCodeBlock extends BasicCodeBlock {
    @Override protected void betweenChunks(LinePrinter lp) {
        lp.line("");
    }
}
