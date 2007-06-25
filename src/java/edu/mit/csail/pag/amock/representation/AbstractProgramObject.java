package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;

public abstract class AbstractProgramObject implements ProgramObject {
    /**
     * For most ProgramObjects, all of the roles are the same and can
     * be implemented with one method.  Note that we assume that
     * getSourceRepresentation does *not* return a matcher; if it
     * does, you must override
     * expectationArgumentRepresentationIsMatcher and
     * getExpectationArgumentRepresentation.
     */
    public abstract String getSourceRepresentation();

    public String getExpectationArgumentRepresentation(boolean forceIntoMatcher,
                                                       Type type) {
        if (forceIntoMatcher) {
            return "with(equal(" + getSourceRepresentation() + "))";
        } else {
            return getSourceRepresentation();
        }
    }

    public String getExpectationReturnValueRepresentation() {
        return "returnValue(" + getSourceRepresentation() + ")";
    }

    public String getPrimaryConstructorArgumentRepresentation() {
        return getPrimaryExecutionArgumentRepresentation();
    }

    public String getPrimaryExecutionArgumentRepresentation() {
        return getSourceRepresentation();
    }

    public String getPrimaryExecutionReturnValueRepresentation() {
        return getSourceRepresentation();
    }

    public String getFieldTweakValueRepresentation() {
        return getSourceRepresentation();
    }

    public boolean expectationArgumentRepresentationIsMatcher() {
        return false;
    }

    public void getsReturnedFromExpectation() {
        // pass
    }
}
