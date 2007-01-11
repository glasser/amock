package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class TestMethodGeneratorTests extends AmockUnitTestCase {
    public void testEmptyMethodGenerator() {
        Mock resolver = mock(ClassNameResolver.class);
        TestMethodGenerator tmg
            = new TestMethodGenerator("cookieEating",
                                      (ClassNameResolver) resolver.proxy());
        Mock app = mock(LinePrinter.class);

        expectLines(app,
                    "public void testCookieEating {",
                    "  // Create mocks.",
                    "  Mock mockCookieJar = mock(CookieJar.class);",
                    "  CookieJar proxyCookieJar = (CookieJar) mockCookieJar.proxy();",
                    "  Mock mockCookie = mock(Cookie.class);",
                    "  Cookie proxyCookie = (Cookie) mockCookie.proxy();",
                    "  Mock mockCookie1 = mock(Cookie.class);",
                    "  Cookie proxyCookie1 = (Cookie) mockCookie1.proxy();",
                    "  ",
                    "  // Set up primary object.",
                    "  CookieMonster testedCookieMonster = new CookieMonster();",
                    "  ",
                    "  // Set up expectations.",
                    "  mockCookieJar.expects(exactly(3))",
                    "    .method(\"getACookie\")",
                    "    .withNoArguments()",
                    "    .will(onConsecutiveCalls(",
                    "      returnValue(proxyCookie),",
                    "      returnValue(proxyCookie1),",
                    "      returnValue(null)",
                    "    ))",
                    "  ;",
                    "  mockCookie.expects(once())",
                    "    .method(\"eat\")",
                    "  ;",
                    "  mockCookie1.expects(once())",
                    "    .method(\"eat\")",
                    "  ;",
                    "  ",
                    "  // Run the code under test.",
                    "}");
        
        resolver.expects(once())
            .method("getSourceName")
            .with(eq("edu.mit.csail.pag.amock.subjects.bakery.CookieJar"))
            .will(returnValue("CookieJar"));
        resolver.expects(exactly(2))
            .method("getSourceName")
            .with(eq("edu.mit.csail.pag.amock.subjects.bakery.Cookie"))
            .will(returnValue("Cookie"));
        resolver.expects(once())
            .method("getSourceName")
            .with(eq("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster"))
            .will(returnValue("CookieMonster"));


        tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.CookieJar");
        tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.Cookie");
        tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.Cookie");

        tmg.addPrimary("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster");
        
        tmg.printSource((LinePrinter) app.proxy());
    }
}
