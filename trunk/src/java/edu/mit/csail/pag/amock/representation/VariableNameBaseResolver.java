package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.util.ClassName;

public interface VariableNameBaseResolver {
    /**
     * Given a fully-qualified (with periods) class name, returns a
     * base (lacking a prefix like mock) to use for an object of that
     * class.  (It will be stateful.)
     */

    public String getVarNameBase(ClassName className);
}