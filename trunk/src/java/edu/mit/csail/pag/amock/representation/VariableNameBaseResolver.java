package edu.mit.csail.pag.amock.representation;

public interface VariableNameBaseResolver {
    /**
     * Given a fully-qualified (with periods) class name, returns a
     * base (lacking a prefix like mock) to use for an object of that
     * class.  (It will be stateful.)
     */

    public String getVarNameBase(String className);
}