package edu.mit.csail.pag.smock.tests;

import java.lang.reflect.Method;
import org.jmock.internal.InvocationExpectation;
import org.jmock.internal.matcher.MockObjectMatcher;
import org.jmock.internal.matcher.MethodMatcher;
import org.jmock.internal.matcher.ParametersMatcher;
import org.jmock.lib.action.ReturnValueAction;
import edu.mit.csail.pag.smock.*;
import edu.mit.csail.pag.amock.jmock.MockObjectTestCase;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.IsEqual.equalTo;
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
            one(BasicSmock.class).noArgsStringI();
            will(returnValue("intercepted"));
        }});

        assertThat(noArgsStringI(), is("intercepted"));
        assertThat(noArgsStringL(), is("left alone"));
    }

    public static String noArgsStringI() {
        return "foo";
    }

    public static String noArgsStringL() {
        return "left alone";
    }
}
