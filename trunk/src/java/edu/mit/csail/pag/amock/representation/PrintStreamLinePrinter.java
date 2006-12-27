package edu.mit.csail.pag.amock.representation;

import java.io.PrintStream;

/**
 * A LinePrinter which wraps a PrintStream.
 */
public class PrintStreamLinePrinter implements LinePrinter {
    private final PrintStream wrapped;

    public PrintStreamLinePrinter(PrintStream wrapped) {
        this.wrapped = wrapped;
    }

    public void line(String s) {
        wrapped.println(s);
    }
}
