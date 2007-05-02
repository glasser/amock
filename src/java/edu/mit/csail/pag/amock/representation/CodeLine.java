package edu.mit.csail.pag.amock.representation;

import java.util.*;

/**
 * An unstructured line of code.
 */
public class CodeLine implements CodeChunk {
    private final String line;
    
    public CodeLine(String line) {
        // We're not going to do any wrapping or anything yet (maybe
        // later).  Also, not dealing with weird characters that might
        // break it: for now, just Don't Do That.
        if (line.contains("\n")) {
            throw new IllegalArgumentException("line can't contain a newline");
        }
        this.line = line;
    }

    public void printSource(LinePrinter lp) {
        lp.line(line);
    }

    public Collection<ProgramObject> getProgramObjects() {
        return Collections.emptySet();
    }
}

 