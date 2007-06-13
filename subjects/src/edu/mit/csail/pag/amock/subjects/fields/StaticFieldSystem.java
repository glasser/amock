package edu.mit.csail.pag.amock.subjects.fields;

import edu.mit.csail.pag.amock.tests.AmockUnitTestCase;

import java.awt.Rectangle;

public class StaticFieldSystem {
    public static void main(String[] args) {
        StaticFieldSystem s = new StaticFieldSystem(Singleton.INSTANCE);
        s.findSecret();
    }

    private final Singleton s;
    
    public StaticFieldSystem(Singleton s) {
        this.s = s;
    }

    public String findSecret() {
        assert this.s == Singleton.INSTANCE;
        return this.s.getField();
    }

    public static class ProcessorTests extends AmockUnitTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
