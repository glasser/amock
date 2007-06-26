package edu.mit.csail.pag.amock.subjects.callback;

public class JokeAudience {
    private final JokeTeller jt;
        
    public JokeAudience(JokeTeller jt) {
        this.jt = jt;
    }

    public String listenToJoke() {
        return jt.knockKnock(this);
    }

    public String whosThere() {
        return jt.response();
    }

    public String who(String knocker) {
        return "they said: " + jt.punchline(knocker);
    }
}
