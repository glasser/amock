package edu.mit.csail.pag.amock.subjects.capture;

public class BouncerQuickClient {
    private final Bouncer r;

    public BouncerQuickClient(Bouncer r) {
        this.r = r;
    }

    public void go() {
        Thing it = new Thing();
        Thing back = r.bounceIt(it);
        assert it == back;
    }
}
