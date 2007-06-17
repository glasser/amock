package edu.mit.csail.pag.smock;

public final class Result {
    public final boolean shortCircuit;
    public final Object shortCircuitReturnValue;

    public Result(boolean shortCircuit, Object shortCircuitReturnValue) {
        this.shortCircuit = shortCircuit;
        this.shortCircuitReturnValue = shortCircuitReturnValue;
    }
}
