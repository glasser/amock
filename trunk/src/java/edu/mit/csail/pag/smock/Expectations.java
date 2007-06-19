package edu.mit.csail.pag.smock;

public class Expectations extends org.jmock.Expectations {
    public <T> T one(Class<T> invokedClass) {
        super.one(CapturingClass.getCapturingClass(invokedClass));
        return null;
    }
}
