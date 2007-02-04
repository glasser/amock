package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes TraceEvents to an XML stream.
 */
public class XMLSerializer extends Serializer {
    private final ObjectOutputStream s;

    public XMLSerializer(OutputStream out) {
        Writer w = new OutputStreamWriter(out);
        XStream xs = new XStream();

        try {
            this.s = xs.createObjectOutputStream(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public void write(TraceEvent o) {
        try {
            s.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public void close() {
        try {
            s.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}