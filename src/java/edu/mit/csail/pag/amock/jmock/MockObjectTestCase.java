package edu.mit.csail.pag.amock.jmock;

import org.jmock.Sequence;
import org.jmock.lib.objenesis.ObjenesisImposteriser;
import java.lang.reflect.*;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(ObjenesisImposteriser.INSTANCE);
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
                    System.err.println("WARNING: Trying to mock class " +
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
                    System.err.println("WARNING: Trying to mock class " +
                                       typeToMock + " with final method: "
                                       + m);
                }
            }
        }
    }
}
