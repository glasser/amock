package edu.mit.csail.pag.amock.tests;

import org.jmock.cglib.MockObjectTestCase;
import org.jmock.Mock;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    /**
     * Expect a line on a Mocked LinePrinter.
     */
    protected void expectLine(Mock lp, String s) {
        lp.expects(once())
            .method("line")
            .with(eq(s))
            .isVoid();
    }
}
    
