package edu.mit.csail.pag.smock;

import org.jmock.syntax.ReceiverClause;
import org.jmock.syntax.MethodClause;
import org.hamcrest.Matcher;

public class SmockReceiverWrapper implements ReceiverClause {
    private final ReceiverClause rc;

    public SmockReceiverWrapper(ReceiverClause rc) {
        this.rc = rc;
    }
    
    public <T> T of(T mockObject) {
        return rc.of(mockObject);
    }

    public MethodClause of(Matcher<Object> objectMatcher) {
        return rc.of(objectMatcher);
    }

    public <T> T of(Class<T> invokedClass) {
        rc.of(CapturingClass.getCapturingClass(invokedClass));
        return null;
    }
}
