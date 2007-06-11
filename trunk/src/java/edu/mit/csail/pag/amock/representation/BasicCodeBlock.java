package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.util.MultiSet;

public class BasicCodeBlock implements CodeBlock {
    private final List<CodeChunk> chunks
        = new ArrayList<CodeChunk>();

    public void addChunk(CodeChunk c) {
        chunks.add(c);
    }

    private void printChunks(LinePrinter lp) {
        boolean first = true;
        for (CodeChunk c : chunks) {
            if (first) {
                first = false;
            } else {
                betweenChunks(lp);
            }

            c.printSource(lp);
        }
    }

    /*
     * Subclasses can override this to do something special in between
     * chunks.
     */
    protected void betweenChunks(LinePrinter lp) {
        // Do nothing, by default.
    }

    /*
     * Subclasses should override this to do something more
     * interesting than just printing the chunks; they should probably
     * call the superclass method at some point.
     */
    public void printSource(LinePrinter lp) {
        printChunks(lp);
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        for (CodeChunk c : chunks) {
            pos.addAll(c.getProgramObjects());
        }
        return pos;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        for (CodeChunk chunk : chunks) {
            chunk.resolveNames(cr, vr);
        }
    }
}
