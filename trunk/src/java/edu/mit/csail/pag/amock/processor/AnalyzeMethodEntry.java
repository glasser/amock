package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

/**
 * Marks unmatched MethodEntrys as fromUninstrumentedCode.
 */
public class AnalyzeMethodEntry implements TraceProcessor<TraceEvent> {
    private final Deserializer<TraceEvent> in;
    private final Serializer<TraceEvent> out;

    private final Map<Integer, Integer> preCallToMethodEntryIds
        = new HashMap<Integer, Integer>();

    private final List<MethodEntry> unmatchedMethodEntrys
        = new ArrayList<MethodEntry>();

    private final List<PreCall> unmatchedPreCalls
        = new ArrayList<PreCall>();

    private PreCall precedingPreCall = null;

    public AnalyzeMethodEntry(Deserializer<TraceEvent> in,
                              Serializer<TraceEvent> out) {
        this.in = in;
        this.out = out;
    }

    public void analyze() {
        in.process(this);

        System.err.format("%d matched, %d unmatched PC, %d unmatched ME\n",
                          preCallToMethodEntryIds.size(),
                          unmatchedPreCalls.size(),
                          unmatchedMethodEntrys.size());
        out.close();
    }

    public void processEvent(TraceEvent ev) {
        if (ev instanceof PreCall) {
            processPreCall((PreCall) ev);
        } else if (ev instanceof MethodEntry) {
            ev = processMethodEntry((MethodEntry) ev);
        } else {
            dealWithPossibleLeftoverPreCall();
        }
        this.out.write(ev);
    }

    private void processPreCall(PreCall ev) {
        dealWithPossibleLeftoverPreCall();

        this.precedingPreCall = ev;
    }

    private MethodEntry processMethodEntry(MethodEntry ev) {
        if (this.precedingPreCall == null) {
            return dealWithUnmatchedMethodEntry(ev);
        } else if (! matches(this.precedingPreCall, ev)) {
            dealWithPossibleLeftoverPreCall();
            return dealWithUnmatchedMethodEntry(ev);
        } else {
            preCallToMethodEntryIds.put(this.precedingPreCall.callId,
                                        ev.callId);
            return ev;
        }
    }

    private MethodEntry dealWithUnmatchedMethodEntry(MethodEntry ev) {
        if (ev.method.name.equals("main") &&
            ev.method.descriptor.equals("([Ljava/lang/String;)V")) {
            // Not too much of a surprise to not match main...
            return ev;
        }

        unmatchedMethodEntrys.add(ev);
        return ev.copyFromUninstrumented();
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
        if (args.length != 2) {
            throw new RuntimeException("usage: AnalyzeMethodEntry trace-in trace-out");
        }

        String inFileName = args[0];
        String outFileName = args[1];

        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(new FileInputStream(inFileName),
                                           TraceEvent.class);
        Serializer<TraceEvent> s
            = Serializer.getSerializer(new FileOutputStream(outFileName));

        new AnalyzeMethodEntry(d, s).analyze();
    }
}
