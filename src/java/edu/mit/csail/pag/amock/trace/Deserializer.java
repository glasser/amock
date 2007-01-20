package edu.mit.csail.pag.amock.trace;

import java.io.InputStream;

public class Deserializer {
    private final XStream xs = new XStream();
    
    private final InputStream inputStream;

    public Deserializer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public TraceEvent read() {
        Object o = xs.fromXML(inputStream);
        if (!(o instanceof TraceEvent)) {
            throw new IllegalStateException("Trace file contains something " +
                                            "not a trace event: " + o);
        }
        return (TraceEvent) o;
    }
}