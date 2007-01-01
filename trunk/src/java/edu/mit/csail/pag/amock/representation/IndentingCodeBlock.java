package edu.mit.csail.pag.amock.representation;

public class IndentingCodeBlock extends AbstractCodeBlock {
    private static final int DEFAULT_INDENTATION = 2;
    
    private final int indentation;

    public IndentingCodeBlock(int indentation) {
        this.indentation = indentation;
    }

    public IndentingCodeBlock() {
        this(DEFAULT_INDENTATION);
    }
    
    public void printSource(LinePrinter lp) {
        LinePrinter indenter = new IndentingLinePrinter(lp, indentation);
        printChunks(indenter);
    }
}
