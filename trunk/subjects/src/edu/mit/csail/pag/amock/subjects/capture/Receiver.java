package edu.mit.csail.pag.amock.subjects.capture;

public class Receiver {
    private Thing it;
    
    public void getIt(Thing it) {
        this.it = it;
    }

    public void getAgain(Thing that) {
        assert this.it == that;
    }

    public Thing whatWasIt() {
        return this.it;
    }
}
