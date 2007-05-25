package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class IdentifyRecordPrimaries {
    private final Deserializer<TraceEvent> deserializer;

    private final Set<Instance> potentialRecordPrimaries
        = new HashSet<Instance>();

    private final Set<Instance> definitelyNotRecordPrimaries
        = new HashSet<Instance>();

    public IdentifyRecordPrimaries(Deserializer<TraceEvent> deserializer) {
        this.deserializer = deserializer;
    }

    public Set<Instance> findRecordPrimaries() {
        while (true) {
            TraceEvent ev = deserializer.read();

            if (ev == null) {
                break;
            }

            processEvent(ev);
        }

        return potentialRecordPrimaries;
    }

    // design question: do we need to care about Instances that are
    // observed only in argument lists/field values (but not
    // receivers)?  For now, we don't.
    private void processEvent(TraceEvent ev) {
        if (ev instanceof FieldRead) {
            processFieldRead((FieldRead) ev);
        } else if (ev instanceof MethodEvent) {
            processMethodEvent((MethodEvent) ev);
        }
    }
    
    private void processFieldRead(FieldRead ev) {
        Instance receiver = ev.receiver;

        if (! RecordPrimaryClassInfo.isRecordPrimaryClass(receiver.className)) {
            return;
        }

        if (definitelyNotRecordPrimaries.contains(receiver)) {
            return;
        }

        potentialRecordPrimaries.add(receiver);
    }

    private void processMethodEvent(MethodEvent ev) {
        // NEXT: good or bad?
    }

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 2) {
            throw new RuntimeException("usage: Processor trace-file rp-file");
        }

        String traceFileName = args[0];
        String rpDump = args[1];
        
        InputStream in = new FileInputStream(traceFileName);
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);

        IdentifyRecordPrimaries p = new IdentifyRecordPrimaries(d);
        Set<Instance> rps = p.findRecordPrimaries();

        PrintStream ps = new PrintStream(rpDump);
        Serializer<Instance> s = Serializer.getSerializer(ps);

        for (Instance rp : rps) {
            s.write(rp);
        }
        
        s.close();
    }
}
