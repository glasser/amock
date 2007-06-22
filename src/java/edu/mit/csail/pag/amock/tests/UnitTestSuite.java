package edu.mit.csail.pag.amock.tests;

import junit.framework.*;

public class UnitTestSuite {
    public static Test suite() {
        TestSuite s = new TestSuite();
        s.addTestSuite(CaptureTests.class);
        s.addTestSuite(HierarchyTests.class);
        s.addTestSuite(TestCaseGeneratorTests.class);
        s.addTestSuite(TestMethodGeneratorTests.class);
        s.addTestSuite(IndentingLinePrinterTests.class);
        s.addTestSuite(PrintStreamLinePrinterTests.class);
        s.addTestSuite(CodeChunkAndBlockTests.class);
        return s;
    }
}
