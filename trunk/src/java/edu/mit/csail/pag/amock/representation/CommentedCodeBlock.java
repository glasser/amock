package edu.mit.csail.pag.amock.representation;

/**
 * A series of chunks with a comment at the top.
 */

public class CommentedCodeBlock extends BasicCodeBlock {
    private final String comment;

    public CommentedCodeBlock(String comment) {
        // We're not going to do any wrapping or anything yet (maybe
        // later).  Also, not dealing with weird characters that might
        // break it: for now, just Don't Do That.
        if (comment.contains("\n")) {
            throw
                new IllegalArgumentException("comment can't contain a newline");
        }
        this.comment = comment;
    }

    public void printSource(LinePrinter lp) {
        lp.line("// " + comment);
        super.printSource(lp);
    }

    public static CommentedCodeBlock decorating(String comment,
                                                CodeChunk chunk) {
        CommentedCodeBlock ccb = new CommentedCodeBlock(comment);
        ccb.addChunk(chunk);
        return ccb;
    }
}
