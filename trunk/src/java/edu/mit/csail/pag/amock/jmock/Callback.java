package edu.mit.csail.pag.amock.jmock;

import org.jmock.lib.action.CustomAction;
import org.jmock.api.Invocation;

public abstract class Callback extends CustomAction {
    public Callback() {
        super("runs callbacks");
    }

    public abstract void go() throws Throwable;

    public Object invoke(Invocation invocation) throws Throwable {
        go();
        return null;
    }
}
