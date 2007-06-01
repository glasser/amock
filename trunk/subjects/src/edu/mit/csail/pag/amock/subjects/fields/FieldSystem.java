package edu.mit.csail.pag.amock.subjects.fields;

import java.io.*;
import java.util.*;

import org.jmock.*;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
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
            public void  testMakeRP() throws FileNotFoundException {
                final TestMethodGenerator tmg = mock(TestMethodGenerator.class);
                final Primary patron = mock(Primary.class);
                final Mocked library = mock(Mocked.class);
                final PrimaryExecution pe = mock(PrimaryExecution.class);
                final Expectation e1 = mock(Expectation.class);
                final Expectation e2 = mock(Expectation.class);
                final RecordPrimary book = mock(RecordPrimary.class);

                checking(new Expectations() {{
                    one (tmg).addPrimary(amockClass("subjects.fields.Patron"),
                                         new ProgramObject[] {},
                                         true);
                    will(returnValue(patron));

                    one (tmg).addMock(amockClass("subjects.fields.Library"));
                    will(returnValue(library));

                    TraceMethod m =
                        new TraceMethod("edu/mit/csail/pag/amock/subjects/fields/Patron",
                                        "browseAndCheckOut",
                                        "(Ledu/mit/csail/pag/amock/subjects/fields/Library;Z)V");
                    one (tmg).addPrimaryExecution(patron, m,
                                                  new ProgramObject[] { library,
                                                                        new Primitive(false) });
                    will(returnValue(pe));

                    // library.browse() -> new Book("Infinite Jest")
                    one (tmg).addExpectation(library, 1); will(returnValue(e1));
                    one (e1).method("browse"); will(returnValue(e1));
                    one (e1).withNoArguments(); will(returnValue(e1));
                    one (e1).returning(book);

                    one (tmg).addRecordPrimary(amockClass("subjects.fields.Book"),
                                               true);
                    will(returnValue(book));
                    
                    TraceField f
                        = new TraceField("edu/mit/csail/pag/amock/subjects/fields/Book",
                                         "title",
                                         "Ljava/lang/String;");
                    one (book).haveFieldValue(f, new Primitive("Infinite Jest"));

                    // library.checkOut("Infinite Jest")
                    one (tmg).addExpectation(library, 1); will(returnValue(e2));
                    one (e2).method("checkOut"); will(returnValue(e2));
                    one (e2).withArguments(new ProgramObject[] { new Primitive("Infinite Jest") });
                    will(returnValue(e2));
                }});

                process("subjects/fields/Patron", tmg);
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
