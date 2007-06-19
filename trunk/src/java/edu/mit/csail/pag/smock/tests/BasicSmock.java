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

            one(BasicSmock.class).noArgsIntegerI();
            will(returnValue(400));

            one(BasicSmock.class).noArgsIntI();
            will(returnValue(300));

            one(BasicSmock.class).addTwoToPrimitiveI(10);
            will(returnValue(480));

            one(BasicSmock.class).addFourToBoxedI(29);
            will(returnValue(189));
        }});

        assertThat(noArgsStringI(), is("intercepted"));
        assertThat(noArgsStringL(), is("left alone"));
        assertThat(noArgsIntegerI(), is(400));
        assertThat(noArgsIntegerL(), is(69));
        assertThat(noArgsIntI(), is(300));
        assertThat(noArgsIntL(), is(23));
        assertThat(addTwoToPrimitiveI(10), is(480));
        assertThat(addThreeToPrimitiveL(15), is(18));
        assertThat(addFourToBoxedI(29), is(189));
        assertThat(addFiveToBoxedL(42), is(47));
    }

    public static String noArgsStringI() {
        assert false : "This should be mocked out!";
        return "foo";
    }

    public static String noArgsStringL() {
        return "left alone";
    }

    public static Integer noArgsIntegerI() {
        assert false : "This should be mocked out!";
        return 42;
    }

    public static Integer noArgsIntegerL() {
        return 69;
    }

    public static int noArgsIntI() {
        assert false : "This should be mocked out!";
        return 94;
    }

    public static int noArgsIntL() {
        return 23;
    }

    public static int addTwoToPrimitiveI(int x) {
        assert false : "This should be mocked out!";
        return x + 2;
    }

    public static int addThreeToPrimitiveL(int x) {
        return x + 3;
    }

    public static int addFourToBoxedI(Integer x) {
        assert false : "This should be mocked out!";
        return x + 4;
    }

    public static int addFiveToBoxedL(Integer x) {
        return x + 5;
    }
}
