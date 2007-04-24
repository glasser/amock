package edu.mit.csail.pag.amock.subjects.fields;

import edu.mit.csail.pag.amock.tests.AmockUnitTestCase;

public class FieldSystem {
    public static void main(String[] args) {
        Book b = new Book("Infinite Jest");
        Library l = new Library(b);
        Patron p = new Patron();

        p.browseAndCheckOut(l);
    }

    public static class ProcessorTests extends AmockUnitTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
