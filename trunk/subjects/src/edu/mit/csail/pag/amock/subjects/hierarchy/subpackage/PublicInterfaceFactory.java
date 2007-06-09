package edu.mit.csail.pag.amock.subjects.hierarchy.subpackage;

import edu.mit.csail.pag.amock.subjects.hierarchy.PublicInterface;

public class PublicInterfaceFactory {
    public PublicInterface createPrivateImplementation() {
        return new PrivateImplementation();
    }

    private class PrivateImplementation implements PublicInterface {
        public void doIt() { }
    }
}
