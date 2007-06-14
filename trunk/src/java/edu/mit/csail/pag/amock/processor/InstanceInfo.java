package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

import java.util.*;
import java.io.Serializable;

/**
 * An InstanceInfo object lists the operations applied to a given
 * Instance during the trace.
 */
public class InstanceInfo implements Serializable {
    /**
     * The Instance that this object tracks.
     */
    public final Instance instance;

    /**
     * All fields read (and ideally set, but this isn't implemented
     * yet) on the Instance. */
    public final Collection<TraceField> usedFields
        = new HashSet<TraceField>();


    /**
     * All methods invoked on the Instance.
     */
    public final Collection<TraceMethod> invokedMethods
        = new HashSet<TraceMethod>();

    /**
     * Static fields that this instance was fetched from.
     */
    public final Collection<TraceField> staticFields
        = new LinkedHashSet<TraceField>();

    public InstanceInfo(Instance instance) {
        this.instance = instance;
    }

    public void fieldUsed(TraceField f) {
        usedFields.add(f);
    }

    public void methodInvoked(TraceMethod m) {
        invokedMethods.add(m);
    }

    public void foundInStaticField(TraceField f) {
        staticFields.add(f);
    }
}
