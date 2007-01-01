package edu.mit.csail.pag.amock.tests;

import org.jmock.Mock;

import edu.mit.csail.pag.amock.representation.*;

public class CodeChunkAndBlockTests extends AmockUnitTestCase {
    public void testCodeLine() {
        Mock lp = mock(LinePrinter.class);
        expectLine(lp, "bla bla bla!");

        (new CodeLine("bla bla bla!"))
            .printSource((LinePrinter) lp.proxy());
    }

    public void testCommentedCodeBlock() {
        Mock lp = mock(LinePrinter.class);

        CodeBlock b = new CommentedCodeBlock("This is a section");
        b.addChunk(new CodeLine("with a line"));
        b.addChunk(new CodeLine("and another"));

        expectLines(lp,
                    "// This is a section",
                    "with a line",
                    "and another");

        b.printSource((LinePrinter) lp.proxy());
    }

    public void testIndentedCodeBlock() {
        Mock lp = mock(LinePrinter.class);

        CodeBlock b = new IndentingCodeBlock(3);
        b.addChunk(new CodeLine("with a line"));
        b.addChunk(new CodeLine("and another"));

        expectLines(lp,
                    "   with a line",
                    "   and another");

        b.printSource((LinePrinter) lp.proxy());
    }

    public void testIndentedAndCommentedBlock() {
        Mock lp = mock(LinePrinter.class);

        CodeBlock ib = new IndentingCodeBlock(7);
        CodeBlock cb = new CommentedCodeBlock("An indented section");
        ib.addChunk(cb);
        cb.addChunk(new CodeLine("with this line"));
        cb.addChunk(new CodeLine("and that one"));

        expectLines(lp,
                    "       // An indented section",
                    "       with this line",
                    "       and that one");

        ib.printSource((LinePrinter) lp.proxy());
    }
}
