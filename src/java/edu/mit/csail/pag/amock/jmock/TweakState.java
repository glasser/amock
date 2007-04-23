package edu.mit.csail.pag.amock.jmock;

import org.jmock.lib.action.CustomAction;
import org.jmock.api.Invocation;

public abstract class TweakState extends CustomAction {
    public TweakState() {
        super("tweaks the state");
    }

    public abstract void go();

    public Object invoke(Invocation invocation) {
        go();
        return null;
    }
}
