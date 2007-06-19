package edu.mit.csail.pag.smock;

public class Expectations extends org.jmock.Expectations {
    public <T> T one(Class<T> invokedClass) {
        CapturingClass cc = CapturingClass.getCapturingClass(invokedClass);
        CapturingClass gotBack = super.one(cc);
        return null;
    }
}
