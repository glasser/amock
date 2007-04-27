package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from a Serializable stream.
 */
public class JavaDeserializer<T> extends Deserializer<T> {
    public JavaDeserializer(InputStream inputStream,
                            Class<T> type) {
        super(type);
        try {
            this.ois = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}