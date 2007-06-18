package edu.mit.csail.pag.smock.tests;

import junit.framework.*;

public class SmockTestSuite {
    public static Test suite() {
        TestSuite s = new TestSuite();
        s.addTestSuite(BasicSmock.class);
        return s;
    }
}
