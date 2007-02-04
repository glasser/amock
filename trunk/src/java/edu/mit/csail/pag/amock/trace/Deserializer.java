package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from a stream.
 */
public abstract class Deserializer {
    public static final boolean USE_XML_SERIALIZATION = true;

    protected ObjectInputStream ois;

    public static Deserializer getDeserializer(InputStream inputStream) {
        if (USE_XML_SERIALIZATION) {
            return new XMLDeserializer(inputStream);
        } else {
            return new JavaDeserializer(inputStream);
        }
    }

    public TraceEvent read() {
        Object o;

        try {
            o = ois.readObject();
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
