package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import org.objectweb.asm.Type;

/**
 * An InternalPrimary is an object which is created by the code under
 * test.  If we're lucky, it just keeps it to itself, or maybe passes
 * it away once and never does anything with it again; otherwise we'll
 * need fancier tricks like Capture.
 */
public class InternalPrimary extends AbstractProgramObject
    implements Primary {
    // Hmm... haven't figured out yet if this should use the "most
    // general type" thing.
    private final ClassName className;

    private String classSourceName = null;
    
    public InternalPrimary(ClassName className) {
        this.className = className;
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(this);
        return pos;
    }

    public String getPrimaryExecutionReceiverRepresentation() {
        throw new RuntimeException("TODO: pull this out of a capture");
    }

    public String getSourceRepresentation() {
        // XXX This is totally hardcoded to the current SVNKit issue:
        // exactly one occurrence of the InternalPrimary, in an
        // argument to an expectation, with no other arguments given
        // (because of the all/nothing matcher thing).
        return "with(a(" + this.classSourceName + ".class))";
    }

    public void usedAsType(Type t) {
        // XXX: could do some checking here of the hierarchy
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.classSourceName == null) {
            this.classSourceName = cr.getSourceName(this.className);
        }
    }

}
