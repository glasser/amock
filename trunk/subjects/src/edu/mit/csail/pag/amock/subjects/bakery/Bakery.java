package edu.mit.csail.pag.amock.subjects.bakery;

// From glasser's MEng thesis proposal.

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;

import org.jmock.InAnyOrder;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.Deserializer;
import edu.mit.csail.pag.amock.trace.Primitive;
import edu.mit.csail.pag.amock.processor.Processor;
import edu.mit.csail.pag.amock.tests.AmockUnitTestCase;

public class Bakery {
    public static void main(String[] args) {
        runTest(new CookieMonster());
        runTest(new NamedCookieMonster("Alistair Cookie"));
        runVoidTest(new VoidingCookieMonster());
    }

    private static void runTest(CookieMonster monster) {
        CookieJar j = new CookieJar();
        Cookie oatmeal = new OatmealCookie();
        j.add(oatmeal);
        loadMoreCookies(j);
        monster.eatAllCookies(j);
    }
    
    private static void runVoidTest(VoidingCookieMonster monster) {
        CookieJar j = new CookieJar();
        Cookie oatmeal = new OatmealCookie();
        j.add(oatmeal);
        loadMoreCookies(j);
        monster.voidlyEatAllCookies(j);
    }
    
    private static void loadMoreCookies(CookieJar j) {
        j.add(new ChocolateCookie());
    }

    public static class ProcessorTests extends AmockUnitTestCase {
        public void testCookieMonster() throws FileNotFoundException {
            InputStream in =
                new FileInputStream("subjects/out/bakery-trace.xml");
            Deserializer d = Deserializer.getDeserializer(in);
            String testedClass =
                "edu/mit/csail/pag/amock/subjects/bakery/CookieMonster";

            final TestMethodGenerator tmg = mock(TestMethodGenerator.class);
            final Primary p = mock(Primary.class);
            final Mocked mJar = mock(Mocked.class);
            final Mocked mC1 = mock(Mocked.class);
            final Mocked mC2 = mock(Mocked.class);
            final PrimaryExecution ass = mock(PrimaryExecution.class);
            final Expectation e1 = mock(Expectation.class);
            final Expectation e2 = mock(Expectation.class);
            final Expectation e3 = mock(Expectation.class);
            final Expectation e4 = mock(Expectation.class);
            final Expectation e5 = mock(Expectation.class);
        
            expects(new InAnyOrder() {{
                one (tmg).addPrimary(amockClass("subjects.bakery.CookieMonster"),
                                     new ProgramObject [] {},
                                     true);
                will(returnValue(p));

                one (tmg).addMock(amockClass("subjects.bakery.CookieJar"));
                will(returnValue(mJar));

                one (tmg).addPrimaryExecution(p, "eatAllCookies",
                                              new ProgramObject[] { mJar });
                will(returnValue(ass));
                one (ass).isEqualTo(new Primitive(2));

                // jar.getACookie() -> OatmealCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e1));
                one (e1).method("getACookie"); will(returnValue(e1));
                one (e1).withNoArguments(); will(returnValue(e1));
                one (tmg).addMock(amockClass("subjects.bakery.OatmealCookie"));
                will(returnValue(mC1));
                one (e1).returning(mC1); will(returnValue(e1));

                // oatmealCookie.eat()
                one (tmg).addExpectation(mC1, 1); will(returnValue(e2));
                one (e2).method("eat"); will(returnValue(e2));
                one (e2).withNoArguments(); will(returnValue(e2));

                // jar.getACookie() -> ChocolateCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e3));
                one (e3).method("getACookie"); will(returnValue(e3));
                one (e3).withNoArguments(); will(returnValue(e3));
                one (tmg).addMock(amockClass("subjects.bakery.ChocolateCookie"));
                will(returnValue(mC2));
                one (e3).returning(mC2); will(returnValue(e3));
            
                // chocolateCookie.eat()
                one (tmg).addExpectation(mC2, 1); will(returnValue(e4));
                one (e4).method("eat"); will(returnValue(e4));
                one (e4).withNoArguments(); will(returnValue(e4));

                // jar.getACookie() -> null
                one (tmg).addExpectation(mJar, 1); will(returnValue(e5));
                one (e5).method("getACookie"); will(returnValue(e5));
                one (e5).withNoArguments(); will(returnValue(e5));
                one (e5).returning(new Primitive(null)); will(returnValue(e5));
            }});

            new Processor(d, tmg, testedClass).process();
        }
        
        public void testNamedCookieMonster() throws FileNotFoundException {
            InputStream in =
                new FileInputStream("subjects/out/bakery-trace.xml");
            Deserializer d = Deserializer.getDeserializer(in);
            String testedClass =
                "edu/mit/csail/pag/amock/subjects/bakery/NamedCookieMonster";

            final TestMethodGenerator tmg = mock(TestMethodGenerator.class);
            final Primary p = mock(Primary.class);
            final Mocked mJar = mock(Mocked.class);
            final Mocked mC1 = mock(Mocked.class);
            final Mocked mC2 = mock(Mocked.class);
            final PrimaryExecution ass = mock(PrimaryExecution.class);
            final Expectation e1 = mock(Expectation.class);
            final Expectation e2 = mock(Expectation.class);
            final Expectation e3 = mock(Expectation.class);
            final Expectation e4 = mock(Expectation.class);
            final Expectation e5 = mock(Expectation.class);
        
            expects(new InAnyOrder() {{
                one (tmg).addPrimary(amockClass("subjects.bakery.NamedCookieMonster"),
                                     new ProgramObject [] {
                                         new Primitive("Alistair Cookie")},
                                     true);
                will(returnValue(p));

                one (tmg).addMock(amockClass("subjects.bakery.CookieJar"));
                will(returnValue(mJar));

                one (tmg).addPrimaryExecution(p, "eatAllCookies",
                                              new ProgramObject[] { mJar });
                will(returnValue(ass));
                one (ass).isEqualTo(new Primitive(2));

                // jar.getACookie() -> OatmealCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e1));
                one (e1).method("getACookie"); will(returnValue(e1));
                one (e1).withNoArguments(); will(returnValue(e1));
                one (tmg).addMock(amockClass("subjects.bakery.OatmealCookie"));
                will(returnValue(mC1));
                one (e1).returning(mC1); will(returnValue(e1));

                // oatmealCookie.eat()
                one (tmg).addExpectation(mC1, 1); will(returnValue(e2));
                one (e2).method("eat"); will(returnValue(e2));
                one (e2).withNoArguments(); will(returnValue(e2));

                // jar.getACookie() -> ChocolateCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e3));
                one (e3).method("getACookie"); will(returnValue(e3));
                one (e3).withNoArguments(); will(returnValue(e3));
                one (tmg).addMock(amockClass("subjects.bakery.ChocolateCookie"));
                will(returnValue(mC2));
                one (e3).returning(mC2); will(returnValue(e3));
            
                // chocolateCookie.eat()
                one (tmg).addExpectation(mC2, 1); will(returnValue(e4));
                one (e4).method("eat"); will(returnValue(e4));
                one (e4).withNoArguments(); will(returnValue(e4));

                // jar.getACookie() -> null
                one (tmg).addExpectation(mJar, 1); will(returnValue(e5));
                one (e5).method("getACookie"); will(returnValue(e5));
                one (e5).withNoArguments(); will(returnValue(e5));
                one (e5).returning(new Primitive(null)); will(returnValue(e5));
            }});

            new Processor(d, tmg, testedClass).process();
        }

        public void testVoidingCookieMonster() throws FileNotFoundException {
            InputStream in =
                new FileInputStream("subjects/out/bakery-trace.xml");
            Deserializer d = Deserializer.getDeserializer(in);
            String testedClass =
                "edu/mit/csail/pag/amock/subjects/bakery/VoidingCookieMonster";

            final TestMethodGenerator tmg = mock(TestMethodGenerator.class);
            final Primary p = mock(Primary.class);
            final Mocked mJar = mock(Mocked.class);
            final Mocked mC1 = mock(Mocked.class);
            final Mocked mC2 = mock(Mocked.class);
            final PrimaryExecution ass = mock(PrimaryExecution.class);
            final Expectation e1 = mock(Expectation.class);
            final Expectation e2 = mock(Expectation.class);
            final Expectation e3 = mock(Expectation.class);
            final Expectation e4 = mock(Expectation.class);
            final Expectation e5 = mock(Expectation.class);
        
            expects(new InAnyOrder() {{
                one (tmg).addPrimary(amockClass("subjects.bakery.VoidingCookieMonster"),
                                     new ProgramObject [] {},
                                     true);
                will(returnValue(p));

                one (tmg).addMock(amockClass("subjects.bakery.CookieJar"));
                will(returnValue(mJar));

                one (tmg).addPrimaryExecution(p, "voidlyEatAllCookies",
                                              new ProgramObject[] { mJar });
                will(returnValue(ass));

                // jar.getACookie() -> OatmealCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e1));
                one (e1).method("getACookie"); will(returnValue(e1));
                one (e1).withNoArguments(); will(returnValue(e1));
                one (tmg).addMock(amockClass("subjects.bakery.OatmealCookie"));
                will(returnValue(mC1));
                one (e1).returning(mC1); will(returnValue(e1));

                // oatmealCookie.eat()
                one (tmg).addExpectation(mC1, 1); will(returnValue(e2));
                one (e2).method("eat"); will(returnValue(e2));
                one (e2).withNoArguments(); will(returnValue(e2));

                // jar.getACookie() -> ChocolateCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e3));
                one (e3).method("getACookie"); will(returnValue(e3));
                one (e3).withNoArguments(); will(returnValue(e3));
                one (tmg).addMock(amockClass("subjects.bakery.ChocolateCookie"));
                will(returnValue(mC2));
                one (e3).returning(mC2); will(returnValue(e3));
            
                // chocolateCookie.eat()
                one (tmg).addExpectation(mC2, 1); will(returnValue(e4));
                one (e4).method("eat"); will(returnValue(e4));
                one (e4).withNoArguments(); will(returnValue(e4));

                // jar.getACookie() -> null
                one (tmg).addExpectation(mJar, 1); will(returnValue(e5));
                one (e5).method("getACookie"); will(returnValue(e5));
                one (e5).withNoArguments(); will(returnValue(e5));
                one (e5).returning(new Primitive(null)); will(returnValue(e5));
            }});

            new Processor(d, tmg, testedClass).process();
        }

    }
}
