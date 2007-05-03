package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class MockAndProxyDeclaration implements CodeChunk {
    private final Mocked mocked;

    public MockAndProxyDeclaration(Mocked mocked, ClassNameResolver resolver) {
        this.mocked = mocked;
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();

        s.append("final ")
            .append(mocked.getClassSourceName())
            .append(" ")
            .append(mocked.getMockVariableName())
            .append(" = mock(")
            .append(mocked.getClassSourceName())
            .append(".class);");
        
        p.line(s.toString());
    }

    public Collection<ProgramObject> getProgramObjects() {
        return Collections.singleton((ProgramObject)mocked);
    }
}
