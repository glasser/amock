package edu.mit.csail.pag.amock.tests;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.jmock.Expectations;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

/**
 * This class serves two purposes: it is a part of the unit test suite
 * which tests the TestMethodGenerator, and when ran as a program it
 * outputs a complete generated test file.
 */

public class TestMethodGeneratorTests extends AmockUnitTestCase {
    private static ClassName d(String c) {
        return ClassName.fromDotted(c);
    }
    private static ClassName s(String c) {
        return ClassName.fromSlashed(c);
    }

    public void testThesisProposalFigure3() {
        final ClassNameResolver resolver = mock(ClassNameResolver.class);
        final LinePrinter app = mock(LinePrinter.class);

        lines(app,
              "public void testCookieEating() throws Throwable {",
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
            one (resolver).getSourceName(d("edu.mit.csail.pag.amock.subjects.bakery.CookieJar"));
            will(returnValue("CookieJar"));

            exactly(2).of (resolver).getSourceName(d("edu.mit.csail.pag.amock.subjects.bakery.Cookie"));
            will(returnValue("Cookie"));

            one (resolver).getSourceName(d("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster"));
            will(returnValue("CookieMonster"));

            one (resolver).getSourceName(d("edu.mit.csail.pag.amock.jmock.Expectations"));
            will(returnValue("Expectations"));

            one (resolver).getStaticMethodName(d("org.hamcrest.core.Is"), "is");
            will(returnValue("is"));

            one (resolver).getStaticMethodName(d("org.hamcrest.MatcherAssert"), "assertThat");
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
        TraceMethod eatAllCookies =
            new TraceMethod(s("edu/mit/csail/pag/amock/subjects/bakery/CookieMonster"),
                            "eatAllCookies",
                            "(Ledu/mit/csail/pag/amock/subjects/bakery/CookieJar;)I");
        TraceMethod getACookie =
            new TraceMethod(s("edu/mit/csail/pag/amock/subjects/bakery/CookieJar"),
                            "getACookie",
                            "()Ledu/mit/csail/pag/amock/subjects/bakery/Cookie;");
        TraceMethod eat =
            new TraceMethod(s("edu/mit/csail/pag/amock/subjects/bakery/Cookie"),
                            "eat",
                            "()V");
        TraceMethod cmConstructor =
            new TraceMethod(s("edu/mit/csail/pag/amock/subjects/bakery/CookieMonster"),
                            "<init>",
                            "()V");
        Primary cm = tmg.addDeclaredPrimary(d("edu.mit.csail.pag.amock.subjects.bakery.CookieMonster"),
                                            cmConstructor,
                                            new ProgramObject[] {});
        tmg.backToMockMode();

        Mocked jar = tmg.addMock(d("edu.mit.csail.pag.amock.subjects.bakery.CookieJar"));
        Mocked c1 = tmg.addMock(d("edu.mit.csail.pag.amock.subjects.bakery.Cookie"));
        Mocked c2 = tmg.addMock(d("edu.mit.csail.pag.amock.subjects.bakery.Cookie"));

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

        tmg.backToMockMode();
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

        