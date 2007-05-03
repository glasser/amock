package edu.mit.csail.pag.amock.representation;

import java.util.*;

/**
 * An unstructured line of code.
 */
public class CodeLine implements CodeChunk {
    private final String line;
    // Can be explicitly set.
    private final Collection<ProgramObject> programObjects;

    public CodeLine(String line) {
        this(line, new HashSet<ProgramObject>());
    }
    
    public CodeLine(String line, Set<ProgramObject> programObjects) {
        // We're not going to do any wrapping or anything yet (maybe
        // later).  Also, not dealing with weird characters that might
        // break it: for now, just Don't Do That.
        if (line.contains("\n")) {
            throw new IllegalArgumentException("line can't contain a newline");
        }
        this.line = line;
        this.programObjects = Collections.unmodifiableSet(programObjects);
    }

    public void printSource(LinePrinter lp) {
        lp.line(line);
    }

    public Collection<ProgramObject> getProgramObjects() {
        return programObjects;
    }
}

 