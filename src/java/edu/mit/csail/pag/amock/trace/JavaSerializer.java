package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes TraceEvents to a Serializable stream.
 */
public class JavaSerializer extends Serializer {
    public JavaSerializer(OutputStream out) {
        try {
            this.oos = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
