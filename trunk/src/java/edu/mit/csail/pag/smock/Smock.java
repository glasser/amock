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
import org.jmock.internal.ReturnDefaultValueAction;
import org.jmock.lib.JavaReflectionImposteriser;
import org.objectweb.asm.Type;
import edu.mit.csail.pag.amock.util.ClassName;

public class Smock {
    public static InvocationDispatcher dispatcher;

    // This is used to make primtive-returning functions return 0,
    // object-returning functions null, etc.
    private static final Action returnifier
        = new ReturnDefaultValueAction(new JavaReflectionImposteriser());
        
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

        Invocation invocation = new Invocation(capturedClass, m, args);

        if (capturedClass.isCapturingExpectations()) {
            // We're in recording mode!  Just record the invocation
            // and make the static call return, um, null.
            capturedClass.recordInvocation(invocation);

            return new Result(true, returnifier.invoke(invocation));
        }

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

    private final static Map<String, Class<?>> PRIMITIVE_CLASSES
        = new HashMap<String, Class<?>>();
    static {
        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("short", short.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("double", double.class);
    }
    
    private static Class<?> getGuaranteedClass(String dottedName) {
        // XXX MUST SUPPORT ARRAY TYPES!
        if (PRIMITIVE_CLASSES.containsKey(dottedName)) {
            return PRIMITIVE_CLASSES.get(dottedName);
        }
        
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
