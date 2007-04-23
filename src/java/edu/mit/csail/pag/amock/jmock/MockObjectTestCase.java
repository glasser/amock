package edu.mit.csail.pag.amock.jmock;

import org.jmock.Sequence;
import org.jmock.lib.objenesis.ObjenesisImposteriser;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class MockObjectTestCase
    extends org.jmock.integration.junit3.MockObjectTestCase {
    {
        setImposteriser(ObjenesisImposteriser.INSTANCE);
    }

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
}
