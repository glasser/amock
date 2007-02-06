package edu.mit.csail.pag.amock.subjects.bakery;

public class VoidingCookieMonster extends CookieMonster {
    public void voidlyEatAllCookies(CookieJar jar) {
        eatAllCookies(jar);
        // no return
    }
}
