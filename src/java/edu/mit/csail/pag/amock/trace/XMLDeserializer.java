package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from an XML stream.
 */
public class XMLDeserializer extends Deserializer {
    private final ObjectInputStream s;

    public XMLDeserializer(InputStream inputStream) {
        Reader r = new InputStreamReader(inputStream);
        XStream xs = new XStream();
        try {
            this.s = xs.createObjectInputStream(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public TraceEvent read() {
        Object o;

        try {
            o = s.readObject();
        } catch (EOFException e) {
            // Out of events.
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        if (!(o instanceof TraceEvent)) {
            throw new IllegalStateException("Trace file contains something " +
                                            "not a trace event: " + o);
        }
        return (TraceEvent) o;
    }
}