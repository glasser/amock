package edu.mit.csail.pag.amock.subjects.bakery;

import java.util.List;

public class CookieJar {
    private List<Cookie> myCookies;
    
    public void add(Cookie c) {
        myCookies.add(c);
    }
    
    public Cookie getACookie() {
        if (myCookies.isEmpty()) {
            return null;
        } else {
            return myCookies.remove(0);
        }
    }
}
