package edu.mit.csail.pag.amock.subjects.capture;

public class ReceiverClient {
    private final Receiver r;

    public ReceiverClient(Receiver r) {
        this.r = r;
    }

    public void go() {
        Thing it = new Thing();
        r.getIt(it);
        r.getAgain(it);
        Thing back = r.whatWasIt();
        assert it == back;
    }
}
