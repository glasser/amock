package edu.mit.csail.pag.amock.representation;

public class IndentingEmptyLineSeparatedCodeBlock extends IndentingCodeBlock {
    CodeBlock block = new EmptyLineSeparatedCodeBlock();
    {
        super.addChunk(block);
    }

    public IndentingEmptyLineSeparatedCodeBlock(int indentation) {
        super(indentation);
    }

    public IndentingEmptyLineSeparatedCodeBlock() {
        super();
    }

    @Override public void addChunk(CodeChunk c) {
        block.addChunk(c);
    }
}
