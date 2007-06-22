package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import org.objectweb.asm.Type;

/**
 * A special implementation of Primary for...
 */
public class InternalPrimary implements Primary {
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

    public String getSourceRepresentation() {
        return "[INTERNAL PRIMARY!!!! " + this.classSourceName + "]";
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
