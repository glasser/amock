package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads objects of a given class from a stream.
 */
public abstract class Deserializer<T> {
    public static final boolean USE_XML_SERIALIZATION = true;

    // Terrible hack to get around the inability to use instanceof or
    // casts on generic types.
    private final Class<T> type;
    
    protected ObjectInputStream ois;

    protected Deserializer(Class<T> type) {
        this.type = type;
    }

    public static <U> Deserializer<U> getDeserializer(InputStream inputStream,
                                                      Class<U> type) {
        if (USE_XML_SERIALIZATION) {
            return new XMLDeserializer<U>(inputStream, type);
        } else {
            return new JavaDeserializer<U>(inputStream, type);
        }
    }

    public T read() {
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
        
        if (!type.isInstance(o)) {
            throw new IllegalStateException("Trace file contains something " +
                                            "of the wrong type (should be "
                                            + type + "): " + o);
        }
        return type.cast(o);
    }
}
