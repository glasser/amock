package edu.mit.csail.pag.amock.trace;

import java.io.*;

public class JavaToXML {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: JavaToXML foo.xml foo.jdump");
            System.exit(1);
        }

        FileInputStream in = new FileInputStream(args[0]);
        PrintStream out = new PrintStream(args[1]);

        Deserializer<Object> d = new JavaDeserializer<Object>(in, Object.class);
        final Serializer<Object> s = new XMLSerializer<Object>(out);

        d.process(new TraceProcessor<Object>() {
                public void processEvent(Object ev) {
                    s.write(ev);
                }});
        s.close();
    }
}
