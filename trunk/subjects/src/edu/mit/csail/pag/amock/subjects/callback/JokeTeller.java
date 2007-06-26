package edu.mit.csail.pag.amock.subjects.callback;

public class JokeTeller {
    private JokeAudience ja;

    public String knockKnock(JokeAudience ja) {
        this.ja = ja;
        return this.ja.whosThere();
    }

    public String response() {
        return this.ja.who("amock");
    }

    public String punchline(String knocker) {
        assert knocker.equals("amock");
        return "amockarena";
    }
}
