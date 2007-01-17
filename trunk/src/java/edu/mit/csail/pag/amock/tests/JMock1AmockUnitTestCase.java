package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;
import edu.mit.csail.pag.amock.representation.*;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.builder.*;

import java.util.*;

public abstract class JMock1AmockUnitTestCase extends MockObjectTestCase {
    private Map<Mock, IDGenerator> idGeneratorsForMocks
        = new HashMap<Mock, IDGenerator>();
        
    /**
     * Expect some lines on a Mocked LinePrinter.
     */
    protected void expectLines(Mock lp, String... strs) {
        for (String s : strs) {
            expectLine(lp, s);
        }
    }

    /**
     * Expect a line on a Mocked LinePrinter, following the last line
     * which went through expectLine on lp.
     */
    protected void expectLine(Mock lp, String s) {
        expectLine(lp, s, lp, getLastID(lp));
    }
    
    /**
     * Expect a line on a Mocked LinePrinter, following lastID on
     * namespace ns.
     */
    protected void expectLine(Mock lp, String s,
                              BuilderNamespace ns,
                              String lastID) {
        String thisId = getThisID(lp);
        
        MatchBuilder mb =
            lp.expects(once()).method("line")
            .with(eq(s));

        if (lastID != null) {
            // It looks better if we don't pass the namespace into
            // after() if it's the same as the mock itself, since
            // otherwise there'd be an extraneous "on mockWhatever" in
            // the description.
            if (lp == ns) {
                mb = mb.after(lastID);
            } else {
                mb = mb.after(ns, lastID);
            }
        }

        mb.id(thisId);
    }

    protected String getLastID(Mock lp) {
        if (idGeneratorsForMocks.containsKey(lp)) {
            return idGeneratorsForMocks.get(lp).getLastID();
        } else {
            return null;
        }
    }

    private String getThisID(Mock lp) {
        if (! idGeneratorsForMocks.containsKey(lp)) {
            idGeneratorsForMocks.put(lp,
                                     new PrefixCountingIDGenerator("line "));
        }
        return idGeneratorsForMocks.get(lp).getNextID();
    }
}
    
