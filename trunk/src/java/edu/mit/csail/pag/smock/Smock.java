package edu.mit.csail.pag.smock;

import org.jmock.api.Invocation;

public class Smock {
    // needs class, args too

    public static Result maybeMockStaticMethod(String className,
                                               String name,
                                               String desc) {
        System.err.format("HELP I AM IN A STATIC METHOD %s.%s!!!\n",
                          className, name);
        if (name.equals("getSomeNumber")) {
            return new Result(true, 38);
        }
        return new Result(false, null);
    }
}
