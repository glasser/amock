package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.representation.*;

public class IndentingLinePrinterTests extends AmockUnitTestCase {
    public void testIndentingLinePrinter() {
        LinePrinter lp = mock(LinePrinter.class);

        IndentingLinePrinter outer
            = new IndentingLinePrinter(lp, 2);
        IndentingLinePrinter inner = new IndentingLinePrinter(outer, 4);

        expects(lines(lp,
                      "  printed from outer",
                      "      printed from inner",
                      "  outer again"));

        outer.line("printed from outer");
        inner.line("printed from inner");
        outer.line("outer again");
    }
}
