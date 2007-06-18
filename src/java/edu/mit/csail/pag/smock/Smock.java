package edu.mit.csail.pag.smock;

import org.jmock.api.Invocation;

public class Smock {
    // needs class, args too

    public static Result maybeMockStaticMethod(String className,
                                               String name,
                                               String desc,
                                               Object[] args) {
        System.err.format("HELP I AM IN A STATIC METHOD %s.%s!%d!!\n",
                          className, name, args.length);
        if (name.equals("getSomeNumber")) {
            return new Result(true, 38);
        }
        return new Result(false, null);
    }
}
