package edu.mit.csail.pag.smock;

public final class Result {
    public final Object shortCircuit;
    public final Object shortCircuitReturnValue;

    public Result(Object shortCircuit, Object shortCircuitReturnValue) {
        this.shortCircuit = shortCircuit;
        this.shortCircuitReturnValue = shortCircuitReturnValue;
    }
}
