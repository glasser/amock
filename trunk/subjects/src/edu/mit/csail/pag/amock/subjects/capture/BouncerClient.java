package edu.mit.csail.pag.amock.subjects.capture;

public class BouncerClient {
    private final Bouncer r;

    public BouncerClient(Bouncer r) {
        this.r = r;
    }

    public void go() {
        Thing it = new Thing();
        Thing back = r.bounceIt(it);
        assert it == back;
        back = r.boing();
        assert it == back;
    }
}
