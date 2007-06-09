package edu.mit.csail.pag.amock.subjects.hierarchy;

import edu.mit.csail.pag.amock.tests.ProcessorTestCase;
import edu.mit.csail.pag.amock.subjects.hierarchy.subpackage.PublicInterfaceFactory;

public class HierarchySystem {
    public static void main(String[] args) {
        PublicInterface p
            = new PublicInterfaceFactory().createPrivateImplementation();
        HierarchySystem hs = new HierarchySystem(p);
        hs.invokeIt();
    }

    private final PublicInterface p;
    public HierarchySystem(PublicInterface p) {
        this.p = p;
    }

    public void invokeIt() {
        this.p.doIt();
    }

    
    public static class ProcessorTests extends ProcessorTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
