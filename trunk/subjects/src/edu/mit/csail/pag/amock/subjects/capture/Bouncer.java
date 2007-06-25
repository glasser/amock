package edu.mit.csail.pag.amock.subjects.capture;

public class Bouncer {
    private Thing it;

    public Thing bounceIt(Thing it) {
        this.it = it;
        return it;
    }

    public Thing boing() {
        return it;
    }
}
