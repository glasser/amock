package edu.mit.csail.pag.amock.subjects.staticmethod;

import edu.mit.csail.pag.amock.subjects.Util;

public class OtherClass {
    public static int getSomeNumber() {
        Util.mustBeMockedOut();
        return 42;
    }
}
