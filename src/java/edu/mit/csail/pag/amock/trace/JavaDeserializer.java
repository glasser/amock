package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from a Serializable stream.
 */
public class JavaDeserializer extends Deserializer {
    public JavaDeserializer(InputStream inputStream) {
        try {
            this.ois = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}