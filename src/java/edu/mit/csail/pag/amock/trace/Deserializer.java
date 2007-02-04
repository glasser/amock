package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from a stream.
 */
public abstract class Deserializer {
    public static final boolean USE_XML_SERIALIZATION = true;

    public static Deserializer getDeserializer(InputStream inputStream) {
        if (USE_XML_SERIALIZATION) {
            return new XMLDeserializer(inputStream);
        } else {
            return null; // XXX
        }
    }

    public abstract TraceEvent read();
}