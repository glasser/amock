package edu.mit.csail.pag.smock;

import java.lang.reflect.Method;
import org.jmock.syntax.ReceiverClause;
import org.jmock.syntax.MethodClause;
import org.jmock.syntax.ParametersClause;
import org.jmock.internal.matcher.MockObjectMatcher;
import org.jmock.internal.InvocationExpectationBuilder;
import org.hamcrest.Matcher;

public class SmockReceiverWrapper implements ReceiverClause {
    private final InvocationExpectationBuilder rc;

    public SmockReceiverWrapper(ReceiverClause rc) {
        this.rc = (InvocationExpectationBuilder) rc;
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

    // intended to be called *before* of
    public PostfixOn method(Matcher<Method> methodMatcher) {
        return new PostfixOn(rc.method(methodMatcher));
    }

    // intended to be called *before* of
    public PostfixOn method(String nameRegex) {
        return new PostfixOn(rc.method(nameRegex));
    }

    public static class PostfixOn {
        private final InvocationExpectationBuilder ieb;

        public PostfixOn(ParametersClause ieb) {
            this.ieb = (InvocationExpectationBuilder) ieb;
        }
        
        public void on(Class<?> cls) {
            ieb.of(new MockObjectMatcher(CapturingClass.getCapturingClass(cls)));
        }
    }
}
