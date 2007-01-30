package edu.mit.csail.pag.amock.subjects.bakery;

// From glasser's MEng thesis proposal.

public class Bakery {
    public static void main(String[] args) {
        runTest(new CookieMonster());
        runTest(new NamedCookieMonster("Alistair Cookie"));
    }

    private static void runTest(CookieMonster monster) {
        CookieJar j = new CookieJar();
        Cookie oatmeal = new OatmealCookie();
        j.add(oatmeal);
        loadMoreCookies(j);
        monster.eatAllCookies(j);
    }
    
    private static void loadMoreCookies(CookieJar j) {
        j.add(new ChocolateCookie());
    }
}
