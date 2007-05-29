package edu.mit.csail.pag.amock.subjects.fields;

import edu.mit.csail.pag.amock.tests.AmockUnitTestCase;

public class FieldSystem {
    public static class MakeMock {
        public static void main(String[] args) {
            doIt(true);
        }
        public static class ProcessorTests extends AmockUnitTestCase {
            // TODO: add processor tests
            public void testNothing() { }
        }
    }
    
    public static class MakeRP {
        public static void main(String[] args) {
            doIt(false);
        }
        public static class ProcessorTests extends AmockUnitTestCase {
            // TODO: add processor tests
            public void testNothing() { }
        }
    }
    
    public static void doIt(boolean readIt) {
        Book b = new Book("Infinite Jest");
        Library l = new Library(b);
        Patron p = new Patron();

        p.browseAndCheckOut(l, readIt);
    }
}
