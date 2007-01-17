package edu.mit.csail.pag.amock.tests;

import org.jmock.InThisOrder;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.nonstd.UnsafeHackConcreteClassImposteriser;
import org.jmock.internal.ExpectationGroupBuilder;

import edu.mit.csail.pag.amock.representation.LinePrinter;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    {
        setImposteriser(new UnsafeHackConcreteClassImposteriser());
    }

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
}
    
