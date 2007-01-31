package edu.mit.csail.pag.amock.tests;

import org.jmock.InThisOrder;
import org.jmock.internal.ExpectationGroupBuilder;

import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;
import edu.mit.csail.pag.amock.representation.LinePrinter;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    /**
     * Expect some lines on a mocked LinePrinter.
     */
    protected ExpectationGroupBuilder lines(final LinePrinter lp,
                                            final String... strs) {
        return new InThisOrder() {{
            for (String s : strs) {
                one (lp).line(s);
            }
        }};
    }

    /**
     * Expect a line on a mocked LinePrinter.
     */
    protected ExpectationGroupBuilder line(LinePrinter lp, String s) {
        return lines(lp, s);
    }

    protected String amockClass(String c) {
        return "edu.mit.csail.pag.amock." + c;
    }
}
    
