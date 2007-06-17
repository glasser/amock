package edu.mit.csail.pag.amock.subjects.staticmethod;

import edu.mit.csail.pag.amock.tests.ProcessorTestCase;

public class SmockSystem {
    public static void main(String[] args) {
        new SmockSystem().doIt();
    }

    public int doIt() {
        return OtherClass.getSomeNumber();
    }
    
    public static class ProcessorTests extends ProcessorTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
