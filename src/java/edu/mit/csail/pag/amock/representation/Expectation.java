package edu.mit.csail.pag.amock.representation;

public class Expectation {
    private final Mocked mocked;
    private final Integer count;

    public Expectation(Mocked mocked, Integer count) {
        this.mocked = mocked;
        this.count = count;
    }

    public Expectation method(String methodName) {
        return this;
    }

    public Expectation withNoArguments() {
        return this;
    }

    public Expectation returningConsecutively(Mocked... returns) {
        return this;
    }
}
