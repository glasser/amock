package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

/**
 * Marks unmatched MethodEntrys as fromUninstrumentedCode.
 */
public class AnalyzeMethodEntry implements TraceProcessor<TraceEvent> {
    private final Deserializer<TraceEvent> deserializer;

    private final Map<Integer, Integer> preCallToMethodEntryIds
        = new HashMap<Integer, Integer>();

    private final List<MethodEntry> unmatchedMethodEntrys
        = new ArrayList<MethodEntry>();

    private final List<PreCall> unmatchedPreCalls
        = new ArrayList<PreCall>();

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

        if (! unmatchedMethodEntrys.isEmpty()) {
            Serializer<MethodStartEvent> s = Serializer.getSerializer(System.out);
            for (MethodEntry m : unmatchedMethodEntrys) {
                s.write(m);
            }
            s.close();
            System.out.println();
        }
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
            dealWithUnmatchedMethodEntry(ev);
        } else if (! matches(this.precedingPreCall, ev)) {
            dealWithPossibleLeftoverPreCall();
            dealWithUnmatchedMethodEntry(ev);
        } else {
            preCallToMethodEntryIds.put(this.precedingPreCall.callId,
                                        ev.callId);
        }
    }

    private void dealWithUnmatchedMethodEntry(MethodEntry ev) {
        if (ev.method.name.equals("main") &&
            ev.method.descriptor.equals("([Ljava/lang/String;)V")) {
            // Not too much of a surprise to not match main...
            return;
        }

        unmatchedMethodEntrys.add(ev);
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

        if (pc.method.isConstructor()) {
            // The MethodEntry will have an uninitialized-receiver,
            // since the fix step doesn't (yet) deal with MethodEntry.
            // But!  It's an INVOKESPECIAL so we can check declaring
            // class at least.
            return pc.method.declaringClass.equals(me.method.declaringClass);
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
