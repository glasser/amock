package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes TraceEvents to a stream.
 */
public abstract class Serializer {
    protected ObjectOutputStream oos;
    
    public static Serializer getSerializer(OutputStream out) {
        if (Deserializer.USE_XML_SERIALIZATION) {
            return new XMLSerializer(out);
        } else {
            return new JavaSerializer(out);
        }
    }

    public void write(TraceEvent o) {
        try {
            oos.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
