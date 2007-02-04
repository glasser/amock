package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes TraceEvents to a stream.
 */
public abstract class Serializer {
    public static Serializer getSerializer(OutputStream out) {
        if (Deserializer.USE_XML_SERIALIZATION) {
            return new XMLSerializer(out);
        } else {
            return null; // XXX
        }
    }

    public abstract void write(TraceEvent o);

    public abstract void close();
}