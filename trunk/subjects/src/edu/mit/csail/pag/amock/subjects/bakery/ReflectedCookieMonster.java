package edu.mit.csail.pag.amock.subjects.bakery;

/**
 * This one gets created via reflection from
 * Class.forName(...).newInstance().  Only a different class so that
 * we can make sure that the processor focuses on it instead of the
 * first CookieMonster.
 */
public class ReflectedCookieMonster extends CookieMonster {}
