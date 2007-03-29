package edu.mit.csail.pag.amock.subjects.bakery;

// From glasser's MEng thesis proposal.

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;

import org.jmock.Expectations;
import org.jmock.Sequence;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
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
        
            checking(new Expectations() {{
                one (tmg).addPrimary(amockClass("subjects.bakery.CookieMonster"),
                                     new ProgramObject [] {},
                                     true);
                will(returnValue(p));

                one (tmg).addMock(amockClass("subjects.bakery.CookieJar"));
                will(returnValue(mJar));

                TraceMethod m =
                    new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieMonster",
                                    "eatAllCookies",
                                    "(Ledu/mit/csail/pag/amock/subjects/bakery/CookieJar;)I");
                one (tmg).addPrimaryExecution(p, m,
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
        
            checking(new Expectations() {{
                one (tmg).addPrimary(amockClass("subjects.bakery.NamedCookieMonster"),
                                     new ProgramObject [] {
                                         new Primitive("Alistair Cookie")},
                                     true);
                will(returnValue(p));

                one (tmg).addMock(amockClass("subjects.bakery.CookieJar"));
                will(returnValue(mJar));

                TraceMethod m =
                    new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieMonster",
                                    "eatAllCookies",
                                    "(Ledu/mit/csail/pag/amock/subjects/bakery/CookieJar;)I");
                one (tmg).addPrimaryExecution(p, m,
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

            final Sequence s = sequence("expectations");
        
            checking(new Expectations() {{
                one (tmg).addPrimary(amockClass("subjects.bakery.VoidingCookieMonster"),
                                     new ProgramObject [] {},
                                     true);
                will(returnValue(p));

                one (tmg).addMock(amockClass("subjects.bakery.CookieJar"));
                will(returnValue(mJar));

                TraceMethod m =
                    new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/VoidingCookieMonster",
                                    "voidlyEatAllCookies",
                                    "(Ledu/mit/csail/pag/amock/subjects/bakery/CookieJar;)V");
                one (tmg).addPrimaryExecution(p, m,
                                              new ProgramObject[] { mJar });
                will(returnValue(ass));

                // jar.getACookie() -> OatmealCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e1));
                inSequence(s);
                one (e1).method("getACookie"); will(returnValue(e1));
                one (e1).withNoArguments(); will(returnValue(e1));
                one (tmg).addMock(amockClass("subjects.bakery.OatmealCookie"));
                will(returnValue(mC1));
                one (e1).returning(mC1); will(returnValue(e1));

                // oatmealCookie.eat()
                one (tmg).addExpectation(mC1, 1); will(returnValue(e2));
                inSequence(s);
                one (e2).method("eat"); will(returnValue(e2));
                one (e2).withNoArguments(); will(returnValue(e2));

                // jar.getACookie() -> ChocolateCookie
                one (tmg).addExpectation(mJar, 1); will(returnValue(e3));
                inSequence(s);
                one (e3).method("getACookie"); will(returnValue(e3));
                one (e3).withNoArguments(); will(returnValue(e3));
                one (tmg).addMock(amockClass("subjects.bakery.ChocolateCookie"));
                will(returnValue(mC2));
                one (e3).returning(mC2); will(returnValue(e3));
            
                // chocolateCookie.eat()
                one (tmg).addExpectation(mC2, 1); will(returnValue(e4));
                inSequence(s);
                one (e4).method("eat"); will(returnValue(e4));
                one (e4).withNoArguments(); will(returnValue(e4));

                // jar.getACookie() -> null
                one (tmg).addExpectation(mJar, 1); will(returnValue(e5));
                inSequence(s);
                one (e5).method("getACookie"); will(returnValue(e5));
                one (e5).withNoArguments(); will(returnValue(e5));
                one (e5).returning(new Primitive(null)); will(returnValue(e5));
            }});

            new Processor(d, tmg, testedClass).process();
        }

        public void testCookieJar() throws FileNotFoundException {
            InputStream in =
                new FileInputStream("subjects/out/bakery-trace.xml");
            Deserializer d = Deserializer.getDeserializer(in);
            String testedClass =
                "edu/mit/csail/pag/amock/subjects/bakery/CookieJar";

            final TestMethodGenerator tmg = mock(TestMethodGenerator.class);
            final Primary pJar = mock(Primary.class);
            final Primary pList = mock(Primary.class);
            final Mocked mC1 = mock(Mocked.class);
            final Mocked mC2 = mock(Mocked.class);
            final PrimaryExecution pe1 = mock(PrimaryExecution.class);
            final PrimaryExecution pe2 = mock(PrimaryExecution.class);
            final PrimaryExecution pe3 = mock(PrimaryExecution.class);
            final PrimaryExecution pe4 = mock(PrimaryExecution.class);
            final PrimaryExecution pe5 = mock(PrimaryExecution.class);
        
            checking(new Expectations() {{
                // Declare primaries.
                one (tmg).addPrimary(amockClass("subjects.bakery.CookieJar"),
                                     new ProgramObject [] {},
                                     true);
                will(returnValue(pJar));

                one (tmg).addPrimary("java.util.ArrayList",
                                     new ProgramObject [] {},
                                     false);
                will(returnValue(pList));


                // Declare mocks.
                one (tmg).addMock(amockClass("subjects.bakery.OatmealCookie"));
                will(returnValue(mC1));

                one (tmg).addMock(amockClass("subjects.bakery.ChocolateCookie"));
                will(returnValue(mC2));

                Sequence s = sequence("primary executations");

                // Create primary executions:
                // add(oatmealCookie)
                TraceMethod m =
                    new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieJar",
                                    "add",
                                    "(Ledu/mit/csail/pag/amock/subjects/bakery/Cookie;)V");
                one (tmg).addPrimaryExecution(pJar, m,
                                              new ProgramObject[] { mC1 });
                will(returnValue(pe1));
                inSequence(s);

                // add(chocolateCookie)
                m = new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieJar",
                                    "add",
                                    "(Ledu/mit/csail/pag/amock/subjects/bakery/Cookie;)V");
                one (tmg).addPrimaryExecution(pJar, m,
                                              new ProgramObject[] { mC2 });
                will(returnValue(pe2));
                inSequence(s);

                // getACookie() three times
                m = new TraceMethod("edu/mit/csail/pag/amock/subjects/bakery/CookieJar",
                                    "getACookie",
                                    "()Ledu/mit/csail/pag/amock/subjects/bakery/Cookie;");
                exactly(3).of (tmg).addPrimaryExecution(pJar, m,
                                              new ProgramObject[] {});
                will(onConsecutiveCalls(returnValue(pe3),
                                        returnValue(pe4),
                                        returnValue(pe5)));
                inSequence(s);

                one (pe3).isEqualTo(mC1);
                one (pe4).isEqualTo(mC2);
                one (pe5).isEqualTo(new Primitive(null));
            }});

            new Processor(d, tmg, testedClass).process();
        }

    }
}
