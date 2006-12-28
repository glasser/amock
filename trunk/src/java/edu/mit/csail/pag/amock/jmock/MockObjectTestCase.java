package edu.mit.csail.pag.amock.jmock;

import org.jmock.core.Constraint;
import org.jmock.core.InvocationMatcher;

public abstract class MockObjectTestCase
    extends org.jmock.cglib.MockObjectTestCase {
    
    public Constraint[] args(Constraint... constraints) {
        return constraints;
    }

    // This name is perhaps confusing, since it sort of overlaps
    // with ArgumentsMatchBuilder.with.
    public InvocationMatcher with(Constraint[]... constraintSequence) {
        return new InvokeCountWithArgumentsMatcher(constraintSequence);
    }
}
