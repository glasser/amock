package edu.mit.csail.pag.amock.tests;

import junit.framework.*;

public class UnitTestSuite {
    public static Test suite() {
        TestSuite s = new TestSuite();
        s.addTestSuite(TestCaseGeneratorTests.class);
        s.addTestSuite(TestMethodGeneratorTests.class);
        s.addTestSuite(IndentingLinePrinterTests.class);
        return s;
    }
}
