package edu.mit.csail.pag.amock.subjects.capture;

import edu.mit.csail.pag.amock.tests.ProcessorTestCase;

public class CaptureSystem {
    public static void main(String[] args) {
        new ReceiverClient(new Receiver()).go();
        // this one should not need a capture:
        new ReceiverQuickClient(new Receiver()).go();
        new BouncerClient(new Bouncer()).go();
        new BouncerQuickClient(new Bouncer()).go();
    }
    
    public static class ProcessorTests extends ProcessorTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
