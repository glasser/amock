package edu.mit.csail.pag.smock.tests;

import java.lang.reflect.Method;
import org.jmock.internal.InvocationExpectation;
import org.jmock.internal.matcher.MockObjectMatcher;
import org.jmock.internal.matcher.MethodMatcher;
import org.jmock.internal.matcher.ParametersMatcher;
import org.jmock.lib.action.ReturnValueAction;
import edu.mit.csail.pag.smock.Smock;
import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BasicSmock extends MockObjectTestCase {
    public void testMonkey() throws Exception {
        checking(Smock.STEAL_DISPATCHER);

        InvocationExpectation e = new InvocationExpectation();
        e.setObjectMatcher(new MockObjectMatcher(BasicSmock.class));
        Method m = getClass().getDeclaredMethod("bla");
        e.setMethodMatcher(new MethodMatcher(m));
        e.setParametersMatcher(new ParametersMatcher(new Object[] {}));
        e.setAction(new ReturnValueAction("intercepted"));
        addExpectation(e);

        assertThat(bla(), is("intercepted"));
        assertThat(beep(), is("left alone"));

        Smock.dispatcher = null;
    }

    public static String bla() {
        return "foo";
    }

    public static String beep() {
        return "left alone";
    }
}
