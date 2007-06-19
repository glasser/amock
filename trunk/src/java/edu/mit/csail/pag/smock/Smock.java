package edu.mit.csail.pag.smock;

import java.util.*;
import java.lang.reflect.Method;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.api.Action;
import org.jmock.api.ExpectationError;
import org.jmock.internal.ExpectationBuilder;
import org.jmock.internal.ExpectationCollector;
import org.jmock.internal.InvocationDispatcher;
import org.objectweb.asm.Type;
import edu.mit.csail.pag.amock.util.ClassName;

public class Smock {
    public static InvocationDispatcher dispatcher;
        
    public static Result maybeMockStaticMethod(String classNameSlashed,
                                               String name,
                                               String desc,
                                               Object[] args) throws Throwable {
        if (dispatcher == null) {
            return new Result(false, null);
        }

        ClassName className = ClassName.fromSlashed(classNameSlashed);

        Class<?> theClass = getGuaranteedClass(className.dotted());
        CapturingClass capturedClass = CapturingClass.getCapturingClass(theClass);

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
        Invocation invocation = new Invocation(capturedClass, m, args);

        try {
            Object result = dispatcher.dispatch(invocation);
            return new Result(true, result);
        } catch (ExpectationError ee) {
            // XXX HACKISH
            if (ee.getMessage().equals("unexpected invocation")) {
                // Just let the call happen normally.
                return new Result(false, null);
            }
            throw ee;
        }
            
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

    public static final ExpectationBuilder STEAL_DISPATCHER
        = new ExpectationBuilder() {
                public void buildExpectations(Action defaultAction,
                                              ExpectationCollector collector) {
                    dispatcher = (InvocationDispatcher) collector;
                }
            };
}