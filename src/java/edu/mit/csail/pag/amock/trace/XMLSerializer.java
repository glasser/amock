package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes TraceEvents to an XML stream.
 */
public class XMLSerializer extends Serializer {
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
