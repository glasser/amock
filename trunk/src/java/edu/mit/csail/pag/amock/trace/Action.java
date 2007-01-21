package edu.mit.csail.pag.amock.trace;

/**
 * Inspired by jpaul.Misc.Action.  A basic void-returning callback.
 */
public interface Action<T> {
    /**
     * Perform some action on the given item.
     */
    public void action(T t);
}
