package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

/**
 * An unstructured line of code.
 */
public class CodeLine implements CodeChunk {
    private final String line;
    // Can be explicitly set.
    private final MultiSet<ProgramObject> programObjects;

    public CodeLine(String line) {
        this(line, new MultiSet<ProgramObject>());
    }
    
    public CodeLine(String line, MultiSet<ProgramObject> programObjects) {
        // We're not going to do any wrapping or anything yet (maybe
        // later).  Also, not dealing with weird characters that might
        // break it: for now, just Don't Do That.
        if (line.contains("\n")) {
            throw new IllegalArgumentException("line can't contain a newline");
        }
        this.line = line;
        this.programObjects = programObjects;
    }

    public void printSource(LinePrinter lp) {
        lp.line(line);
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        return programObjects;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        for (ProgramObject po : programObjects.elementsAsSet()) {
            po.resolveNames(cr, vr);
        }
    }
}

 