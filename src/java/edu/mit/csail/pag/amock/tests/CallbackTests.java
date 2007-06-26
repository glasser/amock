package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.jmock.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CallbackTests extends AmockUnitTestCase {
    private interface JokeTeller {
        public String knockKnock();
        public String response();
        public String punchline(String x);
    }

    private static class JokeAudience {
        private final JokeTeller jt;
        
        public JokeAudience(JokeTeller jt) {
            this.jt = jt;
        }

        public String listenToJoke() {
            return jt.knockKnock();
        }

        public String whosThere() {
            return jt.response();
        }

        public String who(String knocker) {
            return "they said: " + jt.punchline(knocker);
        }
    }

    public void testJokeCallbacks() {
        final JokeTeller jt = mock(JokeTeller.class);
        final JokeAudience ja = new JokeAudience(jt);
        
        checking(new Expectations() {{
            one (jt).knockKnock();
            will(doAll(new Callback() { public void go() {
                assertThat(ja.whosThere(), is("they said: amockarena"));
            }}, returnValue("they said: amockarena")));
            inSequence(s);

            one (jt).response();
            will(doAll(new Callback() { public void go() {
                assertThat(ja.who("amock"), is("they said: amockarena"));
            }}, returnValue("they said: amockarena")));
            inSequence(s);

            one (jt).punchline("amock");
            will(returnValue("amockarena"));
            inSequence(s);
        }});

        assertThat(ja.listenToJoke(), is("they said: amockarena"));
    }
}
