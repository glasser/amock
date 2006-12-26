package edu.mit.csail.pag.amock.subjects.bakery;

public class CookieMonster {
    public void eatAllCookies(CookieJar jar) {
        Cookie k;
        for (k = jar.getACookie();
             k != null;
             k = jar.getACookie()) {
            k.eat();
        }
    }
}
