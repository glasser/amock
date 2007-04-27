package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes objects to an XML stream.
 */
public class XMLSerializer<T> extends Serializer<T> {
    public XMLSerializer(OutputStream out) {
        Writer w = new OutputStreamWriter(out);
        XStream xs = new XStream();

        try {
            this.oos = xs.createObjectOutputStream(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
