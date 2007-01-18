package edu.mit.csail.pag.amock.representation;

public interface ClassNameResolver {
    /**
     * Given a fully-qualified (with periods) class name, returns a
     * name (possibly qualified) that can be used to refer to it.
     *
     * Note that this may be stateful --- ie, it may track which
     * classes have been requested so that requesting source names for
     * two classes of the same name in different packages works.
     */

    public String getSourceName(String longName);

    /**
     * Given a fully-qualified (with periods) class name and a static
     * method, returns a name (possibly qualified) that can be used to
     * refer to the method.  (For example, it might use a static
     * import of the method, or a non-static import of the class.)
     *
     * Note that this may be stateful --- ie, it may track which
     * classes have been requested so that requesting source names for
     * two classes of the same name in different packages works.
     */
    public String getStaticMethodName(String className, String method);

}