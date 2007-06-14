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
public class GatherInstanceInfo implements TraceProcessor<TraceEvent> {
    private final Deserializer<TraceEvent> deserializer;

    private final Map<Instance, InstanceInfo> db
        = new HashMap<Instance, InstanceInfo>();

    public GatherInstanceInfo(Deserializer<TraceEvent> deserializer) {
        this.deserializer = deserializer;
    }

    public Map<Instance, InstanceInfo> gatherInstanceInfo() {
        deserializer.process(this);

        return db;
    }

    public void processEvent(TraceEvent ev) {
        if (ev instanceof FieldRead) {
            processFieldRead((FieldRead) ev);
        } else if (ev instanceof MethodEvent) {
            processMethodEvent((MethodEvent) ev);
        }
    }

    private InstanceInfo getInstanceInfo(Instance i) {
        if (! db.containsKey(i)) {
            db.put(i, new InstanceInfo(i));
        }

        return db.get(i);
    }

    private void ensureInstanceInfoExists(TraceObject to) {
        if (to instanceof Instance) {
            getInstanceInfo((Instance) to);
        }
    }
    
    private void processFieldRead(FieldRead ev) {
        if (ev.isStatic()) {
            processStaticFieldRead(ev);
        } else {
            processInstanceFieldRead(ev);
        }
    }

    private void processStaticFieldRead(FieldRead ev) {
        assert ev.isStatic();

        if (!(ev.value instanceof Instance)) {
            return;
        }
        Instance val = (Instance) ev.value;

        getInstanceInfo(val).foundInStaticField(ev.field);
    }

    private void processInstanceFieldRead(FieldRead ev) {
        assert ! ev.isStatic();
        getInstanceInfo(ev.receiver).fieldUsed(ev.field);
    }

    private void processMethodStartEventArgs(MethodStartEvent ev) {
        for (TraceObject arg : ev.args) {
            ensureInstanceInfoExists(arg);
        }
    }

    private void processReturnValue(PostCall ev) {
        ensureInstanceInfoExists(ev.returnValue);
    }

    private void processPreCallReceiver(PreCall ev) {
        // Could also be String or boxed primitive.
        if (!(ev.receiver instanceof Instance)) {
            return;
        }
        Instance i = (Instance) ev.receiver;

        getInstanceInfo(i).methodInvoked(ev.method);
    }

    private void processMethodEvent(MethodEvent ev) {
        if (ev instanceof MethodStartEvent) {
            processMethodStartEventArgs((MethodStartEvent) ev);
        }
        if (ev instanceof PreCall) {
            processPreCallReceiver((PreCall) ev);
        }
        if (ev instanceof PostCall) {
            processReturnValue((PostCall) ev);
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 2) {
            throw new RuntimeException("usage: GatherInstanceInfo trace-file ii-dump");
        }

        String traceFileName = args[0];
        String iiDump = args[1];
        
        InputStream in = new FileInputStream(traceFileName);
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);

        GatherInstanceInfo g = new GatherInstanceInfo(d);
        Map<Instance, InstanceInfo> iis = g.gatherInstanceInfo();

        PrintStream ps = new PrintStream(iiDump);
        Serializer<InstanceInfo> s
            = Serializer.getSerializer(ps);
        for (InstanceInfo ii : iis.values()) {
            s.write(ii);
        }
        s.close();
    }
}
