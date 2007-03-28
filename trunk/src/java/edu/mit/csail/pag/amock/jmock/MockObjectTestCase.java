package edu.mit.csail.pag.amock.jmock;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(new UnsafeHackConcreteClassImposteriser());
    }
}
