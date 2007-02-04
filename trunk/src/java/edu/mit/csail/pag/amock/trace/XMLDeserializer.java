package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from an XML stream.
 */
public class XMLDeserializer extends Deserializer {
    public XMLDeserializer(InputStream inputStream) {
        Reader r = new InputStreamReader(inputStream);
        XStream xs = new XStream();
        try {
            this.ois = xs.createObjectInputStream(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}