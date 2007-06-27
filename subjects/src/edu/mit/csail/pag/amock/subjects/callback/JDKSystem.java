package edu.mit.csail.pag.amock.subjects.callback;

import java.util.*;
import edu.mit.csail.pag.amock.tests.ProcessorTestCase;

public class JDKSystem {
    public static void main(String[] args) {
        JDKSystem sorter = new JDKSystem(new MyComparator<String>(),
                                         new Adder());
        assert sorter.run() == 1;
    }

    private final Comparator<String> comp;
    private final Adder adder;
    public JDKSystem(Comparator<String> comp, Adder adder) {
        this.comp = comp;
        this.adder = adder;
    }

    public int run() {
        SortedSet<String> s = new TreeSet<String>(this.comp);
        adder.addStuff(s);
        return s.size();
    }
    
    public static class ProcessorTests extends ProcessorTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
