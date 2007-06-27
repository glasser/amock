package edu.mit.csail.pag.amock.trace;

/**
 * Deletes everything that happens during class initialization from
 * the trace.
 */

import java.io.*;
import java.util.*;

public class ClinitTrimmer {
    public static void main(String args[]) throws FileNotFoundException {
        if (args.length != 2) {
            throw new RuntimeException("usage: ClinitTrimmer trace-in.xml trace-out.xml");
        }
        
        String inFileName = args[0];
        String outFileName = args[1];

        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(new FileInputStream(inFileName),
                                           TraceEvent.class);
        Serializer<TraceEvent> s
            = Serializer.getSerializer(new FileOutputStream(outFileName));

        new ClinitTrimmer(d, s).run();
    }

    private final Deserializer<TraceEvent> in;
    private final Serializer<TraceEvent> out;

    public ClinitTrimmer(Deserializer<TraceEvent> in,
                         Serializer<TraceEvent> out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        final Stack<ClinitEntry> context = new Stack<ClinitEntry>();

        in.process(new TraceProcessor<TraceEvent>() {
                public void processEvent(TraceEvent ev) {
                    if (ev instanceof ClinitEntry) {
                        context.push((ClinitEntry) ev);
                    } else if (ev instanceof ClinitExit) {
                        ClinitExit closer = (ClinitExit) ev;
                        
                        if (context.empty()) {
                            throw new RuntimeException("found unmatched "
                                                       + closer);
                        }
                        
                        ClinitEntry opener = context.pop();
                        
                        if (! opener.sameClinit(closer)) {
                            throw new RuntimeException("closer " + closer
                                                       + " not matched by "
                                                       + opener);
                        }

                        // Wee, it matches.  Keep going.
                    } else if (context.empty()) {
                        out.write(ev);
                    }
                }});

        out.close();
    }    
}
