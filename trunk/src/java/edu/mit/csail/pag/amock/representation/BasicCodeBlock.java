package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.util.MultiSet;

public class BasicCodeBlock implements CodeBlock {
    private final List<CodeChunk> chunks
        = new ArrayList<CodeChunk>();

    public void addChunk(CodeChunk c) {
        chunks.add(c);
    }

    private class BetweenChunksTriggerWrapper implements LinePrinter {
        private final LinePrinter lp;
        private boolean printedYet = false;
        
        public BetweenChunksTriggerWrapper(LinePrinter lp) {
            this.lp = lp;
        }

        public void line(String s) {
            if (! printedYet) {
                printedYet = true;
                betweenChunks(lp);
            }
            this.lp.line(s);
        }
    }

    private static class DidPrintWrapper implements LinePrinter {
        private final LinePrinter lp;
        private boolean didPrint = false;
        
        public DidPrintWrapper(LinePrinter lp) {
            this.lp = lp;
        }

        public void line(String s) {
            this.lp.line(s);
            this.didPrint = true;
        }

        public boolean didPrint() {
            return didPrint;
        }
    }

    private void printChunks(LinePrinter lp) {
        boolean printedYet = false;
        for (CodeChunk c : chunks) {
            if (printedYet) {
                c.printSource(new BetweenChunksTriggerWrapper(lp));
            } else {
                DidPrintWrapper dpw = new DidPrintWrapper(lp);
                c.printSource(dpw);
                printedYet = dpw.didPrint();
            }
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
