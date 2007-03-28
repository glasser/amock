package edu.mit.csail.pag.amock.tests;

import org.jmock.Expectations;
import org.jmock.Sequence;

import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;
import edu.mit.csail.pag.amock.representation.LinePrinter;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    protected final Sequence lineSequence = sequence("line printing");
    /**
     * Expect some lines on a mocked LinePrinter.
     */
    protected void lines(final LinePrinter lp,
                         final String... strs) {
        // XXX TODO: ordering not constrained!!!
        checking(new Expectations() {{
            for (String s : strs) {
                one (lp).line(s); inSequence(lineSequence);
            }
        }});
    }

    /**
     * Expect a line on a mocked LinePrinter.
     */
    protected void line(LinePrinter lp, String s) {
        lines(lp, s);
    }

    protected String amockClass(String c) {
        return "edu.mit.csail.pag.amock." + c;
    }
}
    
