package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.TraceObject;
import edu.mit.csail.pag.amock.representation.ProgramObject;

/**
 * (XXX: bad class name?)
 *
 * A BoundaryTranslator converts from TraceObjects to ProgramObjects.
 * Part of its role is deciding which TraceObjects correspond to Mocks
 * and which to Primaries.
 */

public interface BoundaryTranslator {
    /**
     * should have no side effects
     */
    public boolean isKnownPrimary(TraceObject to);
    public boolean isKnownMocked(TraceObject to);

    /**
     * may have side effects
     *
     * isReturnValue is true if this object is being returned from an
     * expectation; this allows more interesting things to happen
     * (like substituting a returnIterator action for a returnValue
     * action).
     */
    public ProgramObject traceToProgram(TraceObject to,
                                        boolean isReturnValue);

    public void setProgramForTrace(TraceObject to, ProgramObject po);
}
