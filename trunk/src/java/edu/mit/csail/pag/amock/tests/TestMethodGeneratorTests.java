package edu.mit.csail.pag.amock.tests;

import java.io.*;
import java.util.*;

import org.jmock.Expectations;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;

/**
 * This class serves two purposes: it is a part of the unit test suite
 * which tests the TestMethodGenerator, and when ran as a program it
 * outputs a complete generated test file.
 */

public class TestMethodGeneratorTests extends AmockUnitTestCase {
    public void testThesisProposalFigure3() {
        final ClassNameResolver resolver = mock(ClassNameResolver.class);
        final LinePrinter app = mock(LinePrinter.class);

        lines(app,
              "public void testCookieEating() {",
              "  // Create mocks.",
              "  final Cookie mockCookie = mock(Cookie.class);",
              "  final Cookie mockCookie1 = mock(Cookie.class);",
              "  final CookieJar mockCookieJar = mock(CookieJar.class);",
              "  ",
              "  // Set up primary object.",
              "  final CookieMonster testedCookieMonster = new CookieMonster();",
              "  ",
              "  // Set up expectations and run the test.",
              "  ",
              "  verifyThenCheck(new Expectations() {{",
              "    one (mockCookieJar).getACookie();",
              "    will(returnValue(mockCookie));",
              "    ",
              "    one (mockCookie).eat();",
              "    ",
              "    one (mockCookieJar).getACookie();",
              "    will(returnValue(mockCookie1));",
              "    ",
              "    one (mockCookie1).eat();",
              "    ",
              "    one (mockCookieJar).getACookie();",
              "    will(returnValue(null));",
              "  }});",
              "  ",
              "  assertThat(testedCookieMonster.eatAllCookies(mockCookieJar),",
              "    is(2)",
              "  );",
              "}");

        checking(new Expectations() {{
            one (resolver).getSourceName("edu.mit.csail.pag.amock.subjects.bakery.CookieJar");
            will(returnValue("CookieJar"));

            exactly(2).of (resolver).getSourceName("edu.mit.csail.pag.amock.subjects.bakery.Cookie");
            will(returnValue("Cookie"));

            one (resolver).getSourceName("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster");
            will(returnValue("CookieMonster"));

            one (resolver).getSourceName("org.jmock.Expectations");
            will(returnValue("Expectations"));

            one (resolver).getStaticMethodName("org.hamcrest.core.Is", "is");
            will(returnValue("is"));

            one (resolver).getStaticMethodName("org.hamcrest.MatcherAssert", "assertThat");
            will(returnValue("assertThat"));;
        }});
        
        TestMethodGenerator tmg
            = new TestMethodGenerator("cookieEating",
                                      new Hierarchy(Collections.<HierarchyEntry>emptySet()));
            
        buildCookieEatingTest(tmg);
        tmg.resolveNames(resolver, tmg);
        tmg.printSource(app);
    }

    private static void buildCookieEatingTest(TestMethodGenerator tmg) {
        Mocked jar = tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.CookieJar");
        Mocked c1 = tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.Cookie");
        Mocked c2 = tmg.addMock("edu.mit.csail.pag.amock.subjects.bakery.Cookie");

        TraceMethod eatAllCookies =
            new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieMonster",
                            "eatAllCookies",
                            "(Ledu/mit/csail/pag/amock/subjects/bakery/CookieJar;)I");
        TraceMethod getACookie =
            new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieJar",
                            "getACookie",
                            "()Ledu/mit/csail/pag/amock/subjects/bakery/Cookie;");
        TraceMethod eat =
            new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/Cookie",
                            "eat",
                            "()V");
        TraceMethod cmConstructor =
            new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieMonster",
                            "<init>",
                            "()V");
        Primary cm = tmg.addPrimary("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster",
                                    cmConstructor,
                                    new ProgramObject[] {},
                                    true);
        tmg.prepareForNewPrimaryExecution();
        tmg.addPrimaryExecution(cm, eatAllCookies, jar)
            .isEqualTo(new Primitive(2));

        tmg.addExpectation(jar, 1)
            .method(getACookie)
            .withNoArguments()
            .returning(c1);
        tmg.addExpectation(c1, 1)
            .method(eat)
            .withNoArguments();
        tmg.addExpectation(jar, 1)
            .method(getACookie)
            .withNoArguments()
            .returning(c2);
        tmg.addExpectation(c2, 1)
            .method(eat)
            .withNoArguments();
        tmg.addExpectation(jar, 1)
            .method(getACookie)
            .withNoArguments()
            .returning(new Primitive(null));
    }
        
    public static void main(String[] args) throws FileNotFoundException {
        TestCaseGenerator tcg = new TestCaseGenerator("CookieMonsterTest");
        TestMethodGenerator tmg
            = new TestMethodGenerator("cookieEating",
                                      new Hierarchy(Collections.<HierarchyEntry>emptySet()));
        tcg.addChunk(tmg);
        
        buildCookieEatingTest(tmg);

        PrintStream out = System.out;
        if (args.length == 1) {
            out = new PrintStream(args[0]);
        }

        tcg.printSource(new PrintStreamLinePrinter(out));
    }
}

        