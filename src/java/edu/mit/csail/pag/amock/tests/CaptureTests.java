package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.jmock.*;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CaptureTests extends AmockUnitTestCase {
    private static interface Receiver {
        void getIt(String s);
        void getAgain(String s);
        String whatWasIt();
    }

    public void testCaptures() {
        final Capture<String> seenTwice = capture(String.class);
        final Receiver r = mock(Receiver.class);
        checking(new Expectations() {{
            one (r).getIt(with(a(String.class)));
            will(seenTwice.capture(0));

            one (r).getAgain(with(valueCapturedBy(seenTwice)));

            one (r).whatWasIt();
            will(returnValueCapturedBy(seenTwice));
        }});

        String random = "I chose: " + new Random().nextInt();
        r.getIt(random);
        r.getAgain(random);
        assertThat(r.whatWasIt(), is(random));
    }

    private static interface Bouncer {
        String bounceIt(String s);
        String boing();
    }

    public void testCaptureAndImmediateReturn() {
        final Capture<String> returnIt = capture(String.class);
        final Bouncer b = mock(Bouncer.class);
        checking(new Expectations() {{
            one (b).bounceIt(with(a(String.class)));
            will(doAll(returnIt.capture(0),
                       returnValueCapturedBy(returnIt)));

            one (b).boing();
            will(returnValueCapturedBy(returnIt));
        }});

        String random = "I chose: " + new Random().nextInt();
        assertThat(b.bounceIt(random), is(random));
        assertThat(b.boing(), is(random));
    }
}
