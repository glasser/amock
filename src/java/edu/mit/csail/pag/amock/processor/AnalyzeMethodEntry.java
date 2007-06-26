package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

/**
 * Reads in a trace and dumps out InstanceInfo data for each Instance
 * mentioned in it.  (Probably should use some sort of DB instead of
 * an in-memory hash.)
 */
public class AnalyzeMethodEntry implements TraceProcessor<TraceEvent> {
    private final Deserializer<TraceEvent> deserializer;

    private final Map<Integer, Integer> preCallToMethodEntryIds
        = new HashMap<Integer, Integer>();

    private final Set<MethodEntry> unmatchedMethodEntrys
        = new HashSet<MethodEntry>();

    private final Set<PreCall> unmatchedPreCalls
        = new HashSet<PreCall>();

    private PreCall precedingPreCall = null;

    public AnalyzeMethodEntry(Deserializer<TraceEvent> deserializer) {
        this.deserializer = deserializer;
    }

    public void analyze() {
        deserializer.process(this);

        System.out.format("%d matched, %d unmatched PC, %d unmatched ME\n",
                          preCallToMethodEntryIds.size(),
                          unmatchedPreCalls.size(),
                          unmatchedMethodEntrys.size());
    }

    public void processEvent(TraceEvent ev) {
        if (ev instanceof PreCall) {
            processPreCall((PreCall) ev);
        } else if (ev instanceof MethodEntry) {
            processMethodEntry((MethodEntry) ev);
        } else {
            dealWithPossibleLeftoverPreCall();
        }
    }

    private void processPreCall(PreCall ev) {
        dealWithPossibleLeftoverPreCall();

        this.precedingPreCall = ev;
    }

    private void processMethodEntry(MethodEntry ev) {
        if (this.precedingPreCall == null) {
            unmatchedMethodEntrys.add(ev);
        } else if (! matches(this.precedingPreCall, ev)) {
            dealWithPossibleLeftoverPreCall();
            unmatchedMethodEntrys.add(ev);
        } else {
            preCallToMethodEntryIds.put(this.precedingPreCall.callId,
                                        ev.callId);
        }
    }

    private boolean matches(PreCall pc, MethodEntry me) {
        // sketchy!
        //
        // Can't just compare directly: PreCall has the static type of
        // the method receiver and MethodEntry has the dynamic type.
        if (! pc.method.hasSameNameAndDescriptor(me.method)) {
            return false;
        }

        // Check args
        if (pc.args.length != me.args.length) {
            return false;
        }

        for (int i = 0; i < pc.args.length; i++) {
            if (! pc.args[i].equals(me.args[i])) {
                return false;
            }
        }

        // Check receiver (or lack thereof)
        if (pc.isStatic() && me.isStatic()) {
            return true;
        }
        
        if (pc.isStatic() || me.isStatic()) {
            return false;
        }
        
        return pc.receiver.equals(me.receiver);
    }

    private void dealWithPossibleLeftoverPreCall() {
        if (this.precedingPreCall != null) {
            unmatchedPreCalls.add(this.precedingPreCall);
            this.precedingPreCall = null;
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 1) {
            throw new RuntimeException("usage: AnalyzeMethodEntry trace-file");
        }

        String traceFileName = args[0];
        
        InputStream in = new FileInputStream(traceFileName);
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);

        AnalyzeMethodEntry g = new AnalyzeMethodEntry(d);
        g.analyze();
    }
}
