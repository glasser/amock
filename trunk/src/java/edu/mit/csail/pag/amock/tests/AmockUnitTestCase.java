package edu.mit.csail.pag.amock.tests;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.nonstd.UnsafeHackConcreteClassImposteriser;

public abstract class AmockUnitTestCase extends MockObjectTestCase {
    {
        setImposteriser(new UnsafeHackConcreteClassImposteriser());
    }
}
    
