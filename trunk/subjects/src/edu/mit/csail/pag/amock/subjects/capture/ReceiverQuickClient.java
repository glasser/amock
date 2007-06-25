package edu.mit.csail.pag.amock.subjects.capture;

public class ReceiverQuickClient {
    private final Receiver r;

    public ReceiverQuickClient(Receiver r) {
        this.r = r;
    }

    public void go() {
        Thing it = new Thing();
        r.getIt(it);
    }
}
