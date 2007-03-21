package edu.mit.csail.pag.amock.subjects.bakery;

public class CookieMonster {
    public int eatAllCookies(CookieJar jar) {
        int cookiesEaten = 0;
        for (Cookie k = jar.getACookie();
             k != null;
             k = jar.getACookie()) {
            k.eat();
            cookiesEaten++;
        }
        return cookiesEaten;
    }
}
