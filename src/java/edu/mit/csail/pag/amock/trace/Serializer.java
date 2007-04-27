package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes objects of a given class to a stream.
 */
public abstract class Serializer<T> {
    protected ObjectOutputStream oos;

    public static <U> Serializer<U> getSerializer(OutputStream out) {
        if (Deserializer.USE_XML_SERIALIZATION) {
            return new XMLSerializer<U>(out);
        } else {
            return new JavaSerializer<U>(out);
        }
    }

    public void write(T o) {
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
