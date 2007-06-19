package edu.mit.csail.pag.smock;

import org.jmock.syntax.ReceiverClause;

/**
 * We want to replace every expectation-creating call in
 * org.jmock.Expectations with one that lets us plug in a Class for
 * static calls in addition to a mock object.  Note that we're not
 * overriding the ones which deal with Matchers for the mock object,
 * since those are irrelevant.
 *
 * Note that parameter matchers have not yet been implemented.
 */
public class Expectations extends org.jmock.Expectations {
    @Override public SmockReceiverWrapper exactly(int count) {
        return new SmockReceiverWrapper(super.exactly(count));
    }
    
    public <T> T one(Class<T> invokedClass) {
        super.one(CapturingClass.getCapturingClass(invokedClass));
        return null;
    }

    @Override public SmockReceiverWrapper atLeast(int count) {
        return new SmockReceiverWrapper(super.atLeast(count));
    }
    
    @Override public SmockReceiverWrapper between(int minCount, int maxCount) {
        return new SmockReceiverWrapper(super.between(minCount, maxCount));
    }
    
    @Override public SmockReceiverWrapper atMost(int count) {
        return new SmockReceiverWrapper(super.atMost(count));
    }

    public <T> T allowing(Class<T> invokedClass) {
        return atLeast(0).of(invokedClass);
    }
    
    public <T> T ignoring(Class<T> invokedClass) {
        return allowing(invokedClass);
    }

    // XXX this might not work, since unlike normal mocks, we let
    // unexpected static calls fall through
    public <T> T never(Class<T> invokedClass) {
        return exactly(0).of(invokedClass);
    }

}
