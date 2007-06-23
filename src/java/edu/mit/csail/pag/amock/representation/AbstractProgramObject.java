package edu.mit.csail.pag.amock.representation;

public abstract class AbstractProgramObject implements ProgramObject {
    /**
     * For most ProgramObjects, all of the roles are the same and can
     * be implemented with one method.  Note that we assume that
     * getSourceRepresentation does *not* return a matcher; if it
     * does, you must override
     * expectationArgumentRepresentationIsMatcher and
     * getExpectationArgumentRepresentationForcedIntoMatcher.
     */
    public abstract String getSourceRepresentation();

    public String getExpectationArgumentRepresentation() {
        return getSourceRepresentation();
    }

    public String getExpectationArgumentRepresentationForcedIntoMatcher() {
        return "with(equal(" + getSourceRepresentation() + "))";
    }

    public String getExpectationReturnValueRepresentation() {
        return getSourceRepresentation();
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
}
