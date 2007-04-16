package edu.mit.csail.pag.amock.trace;

/**
 * When the tracer makes a trace file, it doesn't have an object
 * identity for the receiver of instance initialization methods.  It
 * does by the time it gets to the post-call, though.  In order to
 * allow the main processor to take just one pass, this tool reads in
 * a trace and replaces the dummy constructor-receiver object in
 * constructor pre-call records with the actual constructed instance.
 */

import java.io.*;
import java.util.*;

public class ConstructorFixer {
    public static void main(String args[]) throws FileNotFoundException {
        if (args.length != 2) {
            throw new RuntimeException("usage: ConstructorFixer trace-in.xml trace-out.xml");
        }
        
        String inFileName = args[0];
        String outFileName = args[1];

        // We make two passes, so we need two deserializers.
        Deserializer d1 = Deserializer.getDeserializer(new FileInputStream(inFileName));
        Deserializer d2 = Deserializer.getDeserializer(new FileInputStream(inFileName));
        Serializer s = Serializer.getSerializer(new FileOutputStream(outFileName));

        new ConstructorFixer(d1, d2, s).run();
    }

    private final Deserializer firstIn;
    private final Deserializer secondIn;
    private final Serializer out;

    public ConstructorFixer(Deserializer firstIn,
                            Deserializer secondIn,
                            Serializer out) {
        this.firstIn = firstIn;
        this.secondIn = secondIn;
        this.out = out;
    }

    public void run() {
        Map<Integer,TraceObject> callIdToInstance = new HashMap<Integer,TraceObject>();

        while (true) {
            TraceEvent ev = firstIn.read();

            if (ev == null) {
                break;
            }

            if (!(ev instanceof PostCall)) {
                continue;
            }

            PostCall pc = (PostCall) ev;

            if (!pc.isConstructor()) {
                continue;
            }

            assert !callIdToInstance.containsKey(pc.callId);
            callIdToInstance.put(pc.callId, pc.receiver);
        }

        while (true) {
            TraceEvent ev = secondIn.read();

            if (ev == null) {
                break;
            }

            PreCall pc = (PreCall) ev;

            if (pc.isConstructor()) {
                assert callIdToInstance.containsKey(pc.callId);
                assert pc.receiver instanceof ConstructorReceiver;

                ev = pc.copyWithNewReceiver(callIdToInstance.get(pc.callId));
            }

            out.write(ev);
        }

        out.close();
    }
    
}