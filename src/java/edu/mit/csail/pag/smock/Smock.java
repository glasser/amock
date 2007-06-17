package edu.mit.csail.pag.smock;

public class Smock {
    // needs class, args too

    public static Result maybeMockStaticMethod(String name, String desc) {
        if (name.equals("getSomeNumber")) {
            return new Result(true, 38);
        }
        return new Result(false, null);
    }
}
