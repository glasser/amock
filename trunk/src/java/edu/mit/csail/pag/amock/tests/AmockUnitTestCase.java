package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;
import edu.mit.csail.pag.amock.representation.*;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.builder.*;

import java.util.*;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    private Map<Mock, IDGenerator> idGeneratorsForMocks
        = new HashMap<Mock, IDGenerator>();
        
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
        String lastId = getLastID(lp);
        String thisId = getThisID(lp);
        
        Constraint[][] specs = new Constraint[strs.length][];
        for (int i = 0; i < strs.length; i++) {
            specs[i] = args(eq(strs[i]));
        }

        MatchBuilder mb
            = lp.expects(with(specs))
                .method("line");

        if (lastId != null) {
            mb = mb.after(lastId);
        }

        mb.id(thisId);
    }

    private String getLastID(Mock lp) {
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
    
