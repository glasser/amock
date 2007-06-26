package edu.mit.csail.pag.amock.subjects.callback;

public class JokeAudience {
    private final JokeTeller jt;
    private boolean laughed = false;

    public JokeAudience(JokeTeller jt) {
        this.jt = jt;
    }

    public String listenToJoke() {
        String response = jt.knockKnock(this);
        assert laughed;
        return response;
    }

    public String whosThere() {
        return jt.response();
    }

    public String who(String knocker) {
        laughed = true;
        return "they said: " + jt.punchline(knocker);
    }
}
