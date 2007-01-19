package edu.mit.csail.pag.amock.trace;

import com.thoughtworks.xstream.XStream;
import java.io.PrintStream;

public class Serializer {
    private final XStream xs = new XStream();

    {
        xs.alias("pre-call", PreCall.class);
        xs.alias("post-call", PostCall.class);
        xs.alias("instance", Instance.class);
        xs.alias("primitive", Primitive.class);

        xs.useAttributeFor("id", int.class);
        xs.useAttributeFor("className", String.class);
        xs.useAttributeFor("callId", int.class);
    }

    private final PrintStream ps;

    public Serializer(PrintStream ps) {
        this.ps = ps;
    }

    public void write(Object o) {
        xs.toXML(o, ps);
        ps.println();
    }
}