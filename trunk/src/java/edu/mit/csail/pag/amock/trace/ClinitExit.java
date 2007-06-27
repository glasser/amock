package edu.mit.csail.pag.amock.trace;

import edu.mit.csail.pag.amock.util.*;

public class ClinitExit extends ClinitEvent {
    public ClinitExit(int callId, ClassName className) {
        super(callId, className);
    }
}
