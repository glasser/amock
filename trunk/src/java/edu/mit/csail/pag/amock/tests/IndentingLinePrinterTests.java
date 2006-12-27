package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import edu.mit.csail.pag.amock.representation.*;

public class IndentingLinePrinterTests extends AmockUnitTestCase {
    public void testIndentingLinePrinter() {
        Mock lp = mock(LinePrinter.class);

        IndentingLinePrinter outer
            = new IndentingLinePrinter((LinePrinter) lp.proxy(), 2);
        IndentingLinePrinter inner = new IndentingLinePrinter(outer, 4);

        expectLine(lp, "  printed from outer");
        expectLine(lp, "      printed from inner");
        expectLine(lp, "  outer again");

        outer.line("printed from outer");
        inner.line("printed from inner");
        outer.line("outer again");
    }
}
