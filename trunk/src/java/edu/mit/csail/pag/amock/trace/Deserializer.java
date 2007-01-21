package edu.mit.csail.pag.amock.trace;

import java.io.*;

/**
 * Reads TraceEvents from a stream.
 *
 * Note that if efficiency ends up being a problem, this can be
 * switched to standard Java Serializable (by adding "implements
 * Serializable" to the relevant classes and changing this class).
 */
public class Deserializer {
    private final ObjectInputStream s;

    public Deserializer(InputStream inputStream) {
        Reader r = new InputStreamReader(inputStream);
        XStream xs = new XStream();
        try {
            this.s = xs.createObjectInputStream(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TraceEvent read() {
        Object o;

        try {
            o = s.readObject();
        } catch (EOFException e) {
            // Out of events.
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        if (!(o instanceof TraceEvent)) {
            throw new IllegalStateException("Trace file contains something " +
                                            "not a trace event: " + o);
        }
        return (TraceEvent) o;
    }

    /* For testing. */
    public static void main(String args[]) throws FileNotFoundException {
        InputStream is = new FileInputStream(args[0]);
        Deserializer d = new Deserializer(is);

        while (true) {
            TraceEvent t = d.read();

            if (t == null) {
                break;
            }
            
            System.out.println(t.getClass().getName());
        }
    }
}