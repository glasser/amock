package edu.mit.csail.pag.amock.jmock;

import org.jmock.lib.action.CustomAction;
import org.jmock.api.Invocation;

public class ReturnCapturedValueAction extends CustomAction {
    private final Capture<?> capture;

    public ReturnCapturedValueAction(Capture<?> capture) {
        super("returns a captured value");
        this.capture = capture;
    }

    public Object invoke(Invocation invocation) throws Throwable {
        return capture.getCapturedValue();
    }
}
