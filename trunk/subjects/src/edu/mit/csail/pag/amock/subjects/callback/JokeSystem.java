package edu.mit.csail.pag.amock.subjects.callback;

import edu.mit.csail.pag.amock.tests.ProcessorTestCase;

public class JokeSystem {
    public static void main(String[] args) {
        String joke = new JokeAudience(new JokeTeller()).listenToJoke();
        assert joke.equals("they said: amockarena");
    }
    
    public static class ProcessorTests extends ProcessorTestCase {
        // TODO: add processor tests
        public void testNothing() { }
    }
}
