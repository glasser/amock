package edu.mit.csail.pag.amock.subjects.fields;

import java.io.*;
import java.util.*;

import org.jmock.*;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import edu.mit.csail.pag.amock.processor.Processor;
import edu.mit.csail.pag.amock.tests.ProcessorTestCase;

public class FieldSystem {
    public static class MakeMock {
        public static void main(String[] args) {
            doIt(true);
        }
        public static class ProcessorTests extends ProcessorTestCase {
            // TODO: add processor tests (note, though, that we're
            // assured that readOutLoud is mocked out because it will
            // throw an exception if it's run inside a test)
            public void testNothing() { }
        }
    }
    
    public static class MakeRP {
        public static void main(String[] args) {
            doIt(false);
        }
        public static class ProcessorTests extends ProcessorTestCase {
            private static ClassName d(String c) {
                return ClassName.fromDotted(c);
            }
            private static ClassName s(String c) {
                return ClassName.fromSlashed(c);
            }

            public void  testMakeRP() throws FileNotFoundException {
                final TestMethodGenerator tmg = mock(TestMethodGenerator.class);
                final DeclarablePrimary patron = mock(DeclarablePrimary.class);
                final Mocked library = mock(Mocked.class);
                final PrimaryExecution pe = mock(PrimaryExecution.class);
                final Expectation e1 = mock(Expectation.class);
                final Expectation e2 = mock(Expectation.class);
                final RecordPrimary book = mock(RecordPrimary.class);

                checking(new Expectations() {{
                    TraceMethod constructor =
                        new TraceMethod(s("edu/mit/csail/pag/amock/subjects/fields/Patron"),
                                        "<init>",
                                        "()V");
                    one (tmg).addDeclaredPrimary(amockClass("subjects.fields.Patron"),
                                                 constructor,
                                                 new ProgramObject[] {});
                    will(returnValue(patron));

                    one (tmg).backToMockMode();

                    one (tmg).addMock(amockClass("subjects.fields.Library"));
                    will(returnValue(library));

                    TraceMethod browseAndCheckOut =
                        new TraceMethod(s("edu/mit/csail/pag/amock/subjects/fields/Patron"),
                                        "browseAndCheckOut",
                                        "(Ledu/mit/csail/pag/amock/subjects/fields/Library;Z)V");
                    TraceMethod browse =
                        new TraceMethod(s("edu/mit/csail/pag/amock/subjects/fields/Library"),
                                        "browse",
                                        "()Ledu/mit/csail/pag/amock/subjects/fields/Book;");
                    TraceMethod checkOut =
                        new TraceMethod(s("edu/mit/csail/pag/amock/subjects/fields/Library"),
                                        "checkOut",
                                        "(Ljava/lang/String;)V");
                    
                    one (tmg).addPrimaryExecution(patron, browseAndCheckOut,
                                                  new ProgramObject[] { library,
                                                                        new Primitive(false) });
                    will(returnValue(pe));

                    // library.browse() -> new Book("Infinite Jest")
                    one (tmg).addExpectation(library, 1); will(returnValue(e1));
                    one (e1).method(browse); will(returnValue(e1));
                    one (e1).withNoArguments(); will(returnValue(e1));
                    one (e1).returning(book);

                    one (tmg).addRecordPrimary(amockClass("subjects.fields.Book"));
                    will(returnValue(book));
                    
                    TraceField f
                        = new TraceField(s("edu/mit/csail/pag/amock/subjects/fields/Book"),
                                         "title",
                                         "Ljava/lang/String;");
                    one (book).haveFieldValue(f, new Primitive("Infinite Jest"));

                    // library.checkOut("Infinite Jest")
                    one (tmg).addExpectation(library, 1); will(returnValue(e2));
                    one (e2).method(checkOut); will(returnValue(e2));
                    one (e2).withArguments(new ProgramObject[] { new Primitive("Infinite Jest") });
                    will(returnValue(e2));

                    one (tmg).backToMockMode();
                }});

                process("subjects.fields.Patron", tmg);
            }
        }
    }
    
    public static void doIt(boolean readIt) {
        Book b = new Book("Infinite Jest");
        Library l = new Library(b);
        Patron p = new Patron();

        p.browseAndCheckOut(l, readIt);
    }
}
