package edu.mit.csail.pag.amock.tests;

import java.io.*;

import org.jmock.InAnyOrder;

import edu.mit.csail.pag.amock.representation.*;

/**
 * This class serves two purposes: it is a part of the unit test suite
 * which tests the TestMethodGenerator, and when ran as a program it
 * outputs a complete generated test file.
 */

public class TestMethodGeneratorTests extends AmockUnitTestCase {
    public void testThesisProposalFigure3() {
        final ClassNameResolver resolver = mock(ClassNameResolver.class);
        final LinePrinter app = mock(LinePrinter.class);

        expects(new InAnyOrder() {{
            expects(lines(app,
                          "public void testCookieEating() {",
                          "  // Create mocks.",
                          "  CookieJar mockCookieJar = mock(CookieJar.class);",
                          "  Cookie mockCookie = mock(Cookie.class);",
                          "  Cookie mockCookie1 = mock(Cookie.class);",
                          "  ",
                          "  // Set up primary object.",
                          "  CookieMonster testedCookieMonster = new CookieMonster();",
                          "  ",
                          "  // Set up expectations.",
                          "  expects(new InAnyOrder() {{",
                          "    exactly(3).of (mockCookieJar).getACookie();",
                          "    will(onConsecutiveCalls(",
                          "      returnValue(mockCookie),",
                          "      returnValue(mockCookie1),",
                          "      returnValue(null)",
                          "    ));",
                          "    ",
                          "    one (mockCookie).eat();",
                          "    ",
                          "    one (mockCookie1).eat();",
                          "  }});",
                          "  ",
                          "  // Run the code under test.",
                          "  assertThat(testedCookieMonster.eatAllCookies(mockCookieJar),",
                          "    eq(2)",
                          "  );",
                          "}"));

            one (resolver).getSourceName("edu.mit.csail.pag.amock.subjects.bakery.CookieJar");
            will(returnValue("CookieJar"));

            exactly(2).of (resolver).getSourceName("edu.mit.csail.pag.amock.subjects.bakery.Cookie");
            will(returnValue("Cookie"));

            one (resolver).getSourceName("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster");
            will(returnValue("CookieMonster"));

            one (resolver).getSourceName("org.jmock.InAnyOrder");
            will(returnValue("InAnyOrder"));
        }});
        
        TestMethodGenerator tmg
            = new TestMethodGenerator("cookieEating", resolver);

            
        buildCookieEatingTest(tmg);

        tmg.printSource(app);
    }

    private static void buildCookieEatingTest(TestMethodGenerator tmg) {
        Mocked jar = tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.CookieJar");
        Mocked c1 = tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.Cookie");
        Mocked c2 = tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.Cookie");

        Primary cm = tmg.addPrimary("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster");

        tmg.addExpectation(jar, 3)
            .method("getACookie")
            .withNoArguments()
            .returningConsecutively(c1, c2, null);
        tmg.addExpectation(c1, 1)
            .method("eat")
            .withNoArguments();
        tmg.addExpectation(c2, 1)
            .method("eat")
            .withNoArguments();

        tmg.addAssertion(cm, "eatAllCookies", jar)
            .equalsPrimitive(2);
    }
        
    public static void main(String[] args) throws FileNotFoundException {
        TestCaseGenerator tcg = new TestCaseGenerator("CookieMonsterTest");
        TestMethodGenerator tmg = new TestMethodGenerator("cookieEating",
                                                          tcg);
        tcg.addChunk(tmg);
        
        buildCookieEatingTest(tmg);

        PrintStream out = System.out;
        if (args.length == 1) {
            out = new PrintStream(args[0]);
        }

        tcg.printSource(new PrintStreamLinePrinter(out));
    }
}

        