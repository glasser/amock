package edu.mit.csail.pag.amock.trace;

import java.io.*;

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