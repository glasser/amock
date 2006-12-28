package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    /**
     * Expect a line on a Mocked LinePrinter.
     */
    protected void expectLine(Mock lp, String s) {
        expectLines(lp, s);
    }

    /**
     * Expect some lines on a Mocked LinePrinter.
     */
    protected void expectLines(Mock lp, String... strs) {
        Constraint[][] specs = new Constraint[strs.length][];
        for (int i = 0; i < strs.length; i++) {
            specs[i] = args(eq(strs[i]));
        }
        lp.expects(with(specs))
            .method("line");
    }
}
    
