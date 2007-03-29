package edu.mit.csail.pag.amock.jmock;

import org.jmock.Sequence;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(new UnsafeHackConcreteClassImposteriser());
    }

    protected Sequence s = sequence("ordering");
}
