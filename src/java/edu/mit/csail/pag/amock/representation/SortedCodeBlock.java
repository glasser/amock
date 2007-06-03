package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.util.MultiSet;

public class SortedCodeBlock<T extends CodeChunk> implements CodeBlock {
    private final List<T> chunks
        = new ArrayList<T>();

    private final Comparator<T> comparator;

    // Terrible hack to get around the inability to use instanceof or
    // casts on generic types.
    private final Class<T> type;

    public SortedCodeBlock(Comparator<T> comparator, Class<T> type) {
        this.comparator = comparator;
        this.type = type;
    }

    public void addChunk(CodeChunk c) {
        if (!type.isInstance(c)) {
            throw new IllegalStateException("Attempted to add something not of type "
                                            + type);
        }
        chunks.add(type.cast(c));
    }

    public void printSource(LinePrinter lp) {
        Collections.sort(chunks, comparator);
        for (CodeChunk c : chunks) {
            c.printSource(lp);
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        for (CodeChunk c : chunks) {
            pos.addAll(c.getProgramObjects());
        }
        return pos;
    }
}
