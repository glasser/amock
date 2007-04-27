package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes objects to a Serializable stream.
 */
public class JavaSerializer<T> extends Serializer<T> {
    public JavaSerializer(OutputStream out) {
        try {
            this.oos = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
