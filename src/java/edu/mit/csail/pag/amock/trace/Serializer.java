package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Writes TraceEvents to a stream.
 *
 * Note that if efficiency ends up being a problem, this can be
 * switched to standard Java Serializable (by adding "implements
 * Serializable" to the relevant classes and changing this class).
 */
public class Serializer {
    private final ObjectOutputStream s;

    public Serializer(OutputStream out) {
        Writer w = new OutputStreamWriter(out);
        XStream xs = new XStream();

        try {
            this.s = xs.createObjectOutputStream(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(TraceEvent o) {
        try {
            s.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            s.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}