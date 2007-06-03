package edu.mit.csail.pag.amock.jmock;

import org.hamcrest.Description;
import org.jmock.Sequence;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.legacy.ClassImposteriser;

import java.lang.reflect.*;
import java.util.*;

// XXX
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    // Allow different levels of warnings for things that are tough to
    // mock.  (Probably should use some logging framework.)
    private static final boolean WARN_MOCKED_PUBLIC_FIELDS = true;
    private static final boolean WARN_MOCKED_FINAL_METHODS = false;

    protected Sequence s = sequence("ordering");

    @Override public <T> T mock(Class<T> typeToMock, String name) {
        checkMockability(typeToMock);
        return super.mock(typeToMock, name);
    }

    @Override public <T> T mock(Class<T> typeToMock) {
        checkMockability(typeToMock);
        return super.mock(typeToMock);
    }

    private <T> void checkMockability(Class<T> typeToMock) {
        // Check for public fields, which can be read without us
        // being able to mock them.
        if (WARN_MOCKED_PUBLIC_FIELDS) {
            for (Field f : typeToMock.getFields()) {
                int mods = f.getModifiers();
                if (Modifier.isPublic(mods) && !Modifier.isStatic(mods)) {
                    // XXX maybe only care about non-final fields?
                    System.err.println("WARNING: Trying to mock " +
                                       typeToMock + " with public field: "
                                       + f);
                }
            }
        }

        // Check for final methods, which won't be mocked (and in
        // fact, attempts to set expectations on them will actually
        // call the original method).
        if (WARN_MOCKED_FINAL_METHODS) {
            for (Method m : typeToMock.getMethods()) {
                if (m.getDeclaringClass() == Object.class) {
                    // OK, so there are a bunch of final methods in
                    // java.lang.Object.  We don't care.
                    continue;
                }
                int mods = m.getModifiers();
                if (Modifier.isFinal(mods) && !Modifier.isStatic(mods)) {
                    System.err.println("WARNING: Trying to mock " +
                                       typeToMock + " with final method: "
                                       + m);
                }
            }
        }
    }

    // TODO: needs to be autogenerated into the test suite
    public Action returnFigureEnumeration(Figure... items) {
        return new ReturnFigureEnumerationAction(items);
    }

    public class FigureEnumerationIteratorWrapper implements FigureEnumeration {
        private final Iterator<Figure> it;
        
        public FigureEnumerationIteratorWrapper(Figure... items) {
            this.it = Arrays.asList(items).iterator();
        }

        public boolean hasMoreElements() {
            return it.hasNext();
        }

        public Object nextElement() {
            return it.next();
        }

        public Figure nextFigure() {
            return it.next();
        }
    }

    public class ReturnFigureEnumerationAction implements Action {
        private final Figure[] items;

        public ReturnFigureEnumerationAction(Figure[] items) {
            this.items = items;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            return new FigureEnumerationIteratorWrapper(items);
        }

        public void describeTo(Description description) {
            description.appendValueList("return FigureEnumeration over ",
                                        ", ",
                                        "",
                                        items);
        }
    }
}
