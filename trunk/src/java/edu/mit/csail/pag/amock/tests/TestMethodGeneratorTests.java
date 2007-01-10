package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class TestMethodGeneratorTests extends AmockUnitTestCase {
    public void testEmptyMethodGenerator() {
        Mock resolver = mock(ClassNameResolver.class);
        TestMethodGenerator tmg
            = new TestMethodGenerator("fooAndBar",
                                      (ClassNameResolver) resolver.proxy());
        Mock app = mock(LinePrinter.class);

        expectLines(app,
                    "public void testFooAndBar {",
                    "  // Create mocks.",
                    "  Mock mockCookieJar = mock(CookieJar.class);",
                    "  CookieJar proxyCookieJar = (CookieJar) mockCookieJar.proxy();",
                    "  ",
                    "  // Set up expectations.",
                    "  ",
                    "  // Run the code under test.",
                    "}");
        resolver.expects(once())
            .method("getSourceName")
            .with(eq("edu.mit.csail.pag.amock.subjects.bakery.CookieJar"))
            .will(returnValue("CookieJar"));

        tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.CookieJar");
        
        tmg.printSource((LinePrinter) app.proxy());
    }
}
