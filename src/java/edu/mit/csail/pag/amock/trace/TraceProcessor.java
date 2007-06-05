package edu.mit.csail.pag.amock.trace;

public interface TraceProcessor<T> {
    public void processEvent(T event);
}
