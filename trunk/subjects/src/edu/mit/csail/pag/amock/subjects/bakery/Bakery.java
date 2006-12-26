package edu.mit.csail.pag.amock.subjects.bakery;

// From glasser's MEng thesis proposal.

public class Bakery {
    public static void main(String[] args) {
        CookieJar j = new CookieJar();
        Cookie oatmeal = new OatmealCookie();
        j.add(oatmeal);
        loadMoreCookies(j);
        new CookieMonster().eatAllCookies(j);
    }
    
    private static void loadMoreCookies(CookieJar j) {
        j.add(new ChocolateCookie());
    }
}
