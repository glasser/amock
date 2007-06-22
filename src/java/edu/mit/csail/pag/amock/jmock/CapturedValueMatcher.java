package edu.mit.csail.pag.amock.jmock;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsEqual;

public class CapturedValueMatcher<T> extends BaseMatcher<T> {
    private final Capture<T> capture;

    public CapturedValueMatcher(Capture<T> capture) {
        this.capture = capture;
    }
    
    public boolean matches(Object item) {
        return new IsEqual<T>(this.capture.getCapturedValue()).matches(item);
    }

    public void describeTo(Description description) {
        description.appendText("<captured value>");
    }
}
