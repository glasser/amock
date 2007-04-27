package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from an XML stream.
 */
public class XMLDeserializer<T> extends Deserializer<T> {
    public XMLDeserializer(InputStream inputStream,
                           Class<T> type) {
        super(type);
        Reader r = new InputStreamReader(inputStream);
        XStream xs = new XStream();
        try {
            this.ois = xs.createObjectInputStream(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}