package edu.mit.csail.pag.amock.jmock;

import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class Capture<T> {
    private final Class<T> type;
    private T capturedValue = null;
    private boolean capturedYet = false;

    public Capture(Class<T> type) {
        this.type = type;
    }

    public Action capture(final int whichArg) {
        return new CustomAction("captures argument #" + whichArg) {
            public Object invoke(Invocation i) {
                assert ! capturedYet;
                assert whichArg >= 0 && whichArg < i.getParameterCount();

                capturedValue = type.cast(i.getParameter(whichArg));
                capturedYet = true;
                return null;
            }
        };
    }

    public void captureValue(T caught) {
        assert ! capturedYet;
        capturedValue = caught;
        capturedYet = true;
    }

    public T getCapturedValue() {
        assert capturedYet;
        return capturedValue;
    }

    public boolean didCaptureYet() {
        return capturedYet;
    }
}
