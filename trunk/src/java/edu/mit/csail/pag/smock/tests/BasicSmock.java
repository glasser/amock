package edu.mit.csail.pag.smock.tests;

import java.lang.reflect.Method;
import org.jmock.internal.InvocationExpectation;
import org.jmock.internal.matcher.MockObjectMatcher;
import org.jmock.internal.matcher.MethodMatcher;
import org.jmock.internal.matcher.ParametersMatcher;
import org.jmock.lib.action.ReturnValueAction;
import edu.mit.csail.pag.smock.Smock;
import edu.mit.csail.pag.smock.Expectations;
import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class BasicSmock extends MockObjectTestCase {
    public void setUp() {
        checking(Smock.STEAL_DISPATCHER);
    }

    public void tearDown() {
        Smock.dispatcher = null;
    }
    
    public void testBasic() throws Exception {
        checking(new Expectations() {{
            exactly(1).of(sameInstance((Object)BasicSmock.class))
                .method("bla") // uses regexs!
                .withNoArguments();
            will(returnValue("intercepted"));
        }});

        assertThat(bla(), is("intercepted"));
        assertThat(beep(), is("left alone"));
    }

    public static String bla() {
        return "foo";
    }

    public static String beep() {
        return "left alone";
    }
}
