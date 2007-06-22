package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.jmock.*;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CaptureTests extends AmockUnitTestCase {
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

    private static interface Receiver {
        void getIt(String s);
        void getAgain(String s);
        String whatWasIt();
    }
}
