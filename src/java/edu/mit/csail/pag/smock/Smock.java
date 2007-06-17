package edu.mit.csail.pag.smock;

public class Smock {
    // needs class, args too
    private static Object NOT_NULL = new Object();
    public static Result maybeMockStaticMethod(String name, String desc) {
        if (name.equals("getSomeNumber")) {
            return new Result(NOT_NULL, "asdf");
        }
        return new Result(null, null);
    }
}
