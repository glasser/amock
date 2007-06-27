package edu.mit.csail.pag.amock.trace;

import edu.mit.csail.pag.amock.util.*;

public abstract class ClinitEvent extends TraceEvent {
    public final int callId;
    public final ClassName className;

    public ClinitEvent(int callId, ClassName className) {
        this.callId = callId;
        this.className = className;
    }

    public boolean sameClinit(ClinitEvent ev) {
        return this.callId == ev.callId &&
            this.className.equals(ev.className);
    }

    @Override public String toString() {
        return String.format("[%s#%d: %s]",
                             this.getClass().getSimpleName(),
                             this.callId,
                             this.className.dotted());
    }
}
