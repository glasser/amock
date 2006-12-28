package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import org.jmock.core.Constraint;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.jmock.*;

import java.util.*;

public class IndentingLinePrinterTests extends AmockUnitTestCase {
    public void testIndentingLinePrinter() {
        Mock lp = mock(LinePrinter.class);

        IndentingLinePrinter outer
            = new IndentingLinePrinter((LinePrinter) lp.proxy(), 2);
        IndentingLinePrinter inner = new IndentingLinePrinter(outer, 4);

        expectLines(lp,
                    "  printed from outer",
                    "      printed from inner",
                    "  outer again");

        outer.line("printed from outer");
        inner.line("printed from inner");
        outer.line("outer again");
    }
}
