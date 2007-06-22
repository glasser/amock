package edu.mit.csail.pag.amock.jmock;

import org.hamcrest.Matcher;
import org.jmock.api.Action;

public class Expectations extends edu.mit.csail.pag.smock.Expectations {
    public Action returnValueCapturedBy(Capture<?> capture) {
        return new ReturnCapturedValueAction(capture);
    }

    public <T> Matcher<T> valueCapturedBy(Capture<T> capture) {
        return new CapturedValueMatcher<T>(capture);
    }
}
