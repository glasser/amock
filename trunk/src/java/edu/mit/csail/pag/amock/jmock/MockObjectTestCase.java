package edu.mit.csail.pag.amock.jmock;

import org.jmock.lib.nonstd.UnsafeHackConcreteClassImposteriser;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(new UnsafeHackConcreteClassImposteriser());
    }
}
