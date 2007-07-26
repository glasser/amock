package edu.mit.csail.pag.amock.subjects.bakery;

/**
 * Tests what happens if the constructor has expectations.
 */

public class EagerCookieMonster extends CookieMonster {
    public EagerCookieMonster(CookieJar jar) {
        eatAllCookies(jar);
    }
}
