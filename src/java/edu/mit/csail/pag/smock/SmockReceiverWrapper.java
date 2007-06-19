package edu.mit.csail.pag.smock;

import java.lang.reflect.Method;
import org.jmock.syntax.ReceiverClause;
import org.jmock.syntax.MethodClause;
import org.jmock.syntax.ParametersClause;
import org.jmock.internal.matcher.MockObjectMatcher;
import org.jmock.internal.InvocationExpectationBuilder;
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
