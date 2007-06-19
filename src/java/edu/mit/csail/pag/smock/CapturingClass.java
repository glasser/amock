package edu.mit.csail.pag.smock;

import org.jmock.internal.CaptureControl;
import org.jmock.internal.ExpectationCapture;

import edu.mit.csail.pag.amock.util.ClassName;

import java.util.*;

/**
 * Must be interned to work!
 */

public class CapturingClass implements CaptureControl {
    private final static Map<Class<?>, CapturingClass> CACHE
        = new HashMap<Class<?>, CapturingClass>();
        
    public final Class<?> cls;

    private CapturingClass(Class<?> cls) {
        this.cls = cls;
    }

    public static CapturingClass getCapturingClass(Class<?> c) {
        if (! CACHE.containsKey(c)) {
            CACHE.put(c, new CapturingClass(c));
        }
        return CACHE.get(c);
    }

    public void startCapturingExpectations(ExpectationCapture capture) {
        // XXX
    }

    public void stopCapturingExpectations() {
        // XXX
    }
}
