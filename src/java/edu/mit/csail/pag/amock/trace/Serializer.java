package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;

public class Serializer {
    private final XStream xs = new XStream();
    
    private final PrintStream ps;

    public Serializer(PrintStream ps) {
        this.ps = ps;
    }

    public void write(TraceEvent o) {
        xs.toXML(o, ps);
        ps.println();
    }
}