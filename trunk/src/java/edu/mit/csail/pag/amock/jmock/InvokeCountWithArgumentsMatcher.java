package edu.mit.csail.pag.amock.jmock;

import org.jmock.core.matcher.InvokeCountMatcher;
import org.jmock.core.matcher.ArgumentsMatcher;
import org.jmock.core.Constraint;
import org.jmock.core.Invocation;

import java.util.*;

public class InvokeCountWithArgumentsMatcher extends InvokeCountMatcher {
    private final List<ArgumentsMatcher> argumentsMatchers;
    
    public InvokeCountWithArgumentsMatcher(Constraint[][] callSequence) {
        super(callSequence.length);

        argumentsMatchers = new ArrayList<ArgumentsMatcher>();
        for (Constraint[] constraints : callSequence) {
            argumentsMatchers.add(new ArgumentsMatcher(constraints));
        }
    }

    public boolean matches(Invocation invocation) {
        if (! super.matches(invocation)) {
            // Called too many times!
            return false;
        }

        ArgumentsMatcher m = argumentsMatchers.get(getInvocationCount());
        return m.matches(invocation);
    }

    public StringBuffer describeTo(StringBuffer b) {
        return super.describeTo(b).append(" (TODO: with some specified arguments)");
    }            
}
