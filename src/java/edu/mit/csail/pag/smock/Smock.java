package edu.mit.csail.pag.smock;

import java.lang.reflect.Method;
import org.jmock.api.Invocation;
import org.objectweb.asm.Type;
import edu.mit.csail.pag.amock.util.ClassName;

public class Smock {
    // needs class, args too

    public static Result maybeMockStaticMethod(String classNameSlashed,
                                               String name,
                                               String desc,
                                               Object[] args) {
        ClassName className = ClassName.fromSlashed(classNameSlashed);

        Class<?> theClass = getGuaranteedClass(className.dotted());

        Type[] argTypes = Type.getArgumentTypes(desc);

        Class[] argClasses = new Class[argTypes.length];

        for (int i = 0; i < argTypes.length; i++) {
            argClasses[i] = getGuaranteedClass(argTypes[i].getClassName());
        }
        
        Method m;

        try {
            m = theClass.getDeclaredMethod(name, argClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // Use of the class as the invoked object is kind of a hack,
        // but we won't install an object matcher anyway.
        Invocation invocation = new Invocation(theClass, m, args);
        
        System.err.format("HELP I AM IN A STATIC METHOD %s.%s!%d!%s!\n",
                          className.dotted(), name, args.length, m);
        if (name.equals("getSomeNumber")) {
            return new Result(true, 38);
        }
        return new Result(false, null);
    }

    private static Class<?> getGuaranteedClass(String dottedName) {
        try {
            return Class.forName(dottedName);
        } catch (ClassNotFoundException e) {
            // Shouldn't happen: we're called from a method in this
            // class.
            throw new RuntimeException(e);
        }
    }

}
