package edu.mit.csail.pag.amock.trace;

import edu.mit.csail.pag.amock.util.*;

public abstract class ClinitEvent extends TraceEvent {
    public final int callId;
    public final ClassName className;

    public ClinitEvent(int callId, ClassName className) {
        this.callId = callId;
        this.className = className;
    }
}
