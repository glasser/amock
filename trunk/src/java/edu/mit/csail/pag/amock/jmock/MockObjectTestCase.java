package edu.mit.csail.pag.amock.jmock;

import org.jmock.Sequence;
import org.jmock.lib.objenesis.ObjenesisImposteriser;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(ObjenesisImposteriser.INSTANCE);
    }

    protected Sequence s = sequence("ordering");
}
