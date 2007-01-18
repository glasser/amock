package edu.mit.csail.pag.amock.tests;

import org.jmock.Mock;

import edu.mit.csail.pag.amock.representation.*;

public class CodeChunkAndBlockTests extends AmockUnitTestCase {
    public void testCodeLine() {
        LinePrinter lp = mock(LinePrinter.class);
        
        expects(line(lp, "bla bla bla!"));

        (new CodeLine("bla bla bla!")).printSource(lp);
    }

    public void testCommentedCodeBlock() {
        LinePrinter lp = mock(LinePrinter.class);

        CodeBlock b = new CommentedCodeBlock("This is a section");
        b.addChunk(new CodeLine("with a line"));
        b.addChunk(new CodeLine("and another"));

        expects(lines(lp,
                      "// This is a section",
                      "with a line",
                      "and another"));
        
        b.printSource(lp);
    }

    public void testIndentedCodeBlock() {
        LinePrinter lp = mock(LinePrinter.class);

        CodeBlock b = new IndentingCodeBlock(3);
        b.addChunk(new CodeLine("with a line"));
        b.addChunk(new CodeLine("and another"));

        expects(lines(lp,
                      "   with a line",
                      "   and another"));

        b.printSource(lp);
    }

    public void testIndentedAndCommentedBlock() {
        LinePrinter lp = mock(LinePrinter.class);

        CodeBlock ib = new IndentingCodeBlock(7);
        CodeBlock cb = new CommentedCodeBlock("An indented section");
        ib.addChunk(cb);
        cb.addChunk(new CodeLine("with this line"));
        cb.addChunk(new CodeLine("and that one"));

        expects(lines(lp,
                      "       // An indented section",
                      "       with this line",
                      "       and that one"));

        ib.printSource(lp);
    }

    public void testEmptyLineSeparatedBlock() {
        LinePrinter lp = mock(LinePrinter.class);

        CodeBlock b = new EmptyLineSeparatedCodeBlock();
        b.addChunk(new CodeLine("first"));
        b.addChunk(new CodeLine("second"));
        b.addChunk(new CodeLine("third"));

        expects(lines(lp,
                      "first",
                      "",
                      "second",
                      "",
                      "third"));

        b.printSource(lp);
    }
}
