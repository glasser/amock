package edu.mit.csail.pag.amock.representation;

/**
 * Anything that might contain class or variable names that may need
 * to be resolved.
 */
public interface NameContainer {
    /**
     * Asks the object to resolve any class or variable names inside
     * it using the provided resolvers.
     */
    public void resolveNames(ClassNameResolver cr, VariableNameBaseResolver vr);
}
