package edu.mit.csail.pag.amock.representation;

/**
 * A LinePrinter which wraps another LinePrinter, adding
 * an indentation to the front.
 */
public class IndentingLinePrinter implements LinePrinter {
    private final String prefix;
    private final LinePrinter wrapped;

    public IndentingLinePrinter(LinePrinter wrapped, int indent) {
        this.wrapped = wrapped;

        StringBuilder sb = new StringBuilder(indent);
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }

        this.prefix = sb.toString();
    }

    public void line(String s) {
        wrapped.line(prefix + s);
    }
}
