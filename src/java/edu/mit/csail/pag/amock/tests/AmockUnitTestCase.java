package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.jmock.Expectations;
import org.jmock.Sequence;

import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;
import edu.mit.csail.pag.amock.representation.LinePrinter;
import edu.mit.csail.pag.amock.util.*;

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

    protected ClassName amockClass(String c) {
        return ClassName.fromDotted("edu.mit.csail.pag.amock." + c);
    }
}
    
