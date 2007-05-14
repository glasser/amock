package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class MockAndProxyDeclaration implements CodeChunk {
    private final Mocked mocked;

    public MockAndProxyDeclaration(Mocked mocked, ClassNameResolver resolver) {
        this.mocked = mocked;
    }

    public void printSource(LinePrinter p) {
        if (mocked.needsDeclaration()) {
            StringBuilder s = new StringBuilder();
            
            s.append("final ")
                .append(mocked.getClassSourceName())
                .append(" ")
                .append(mocked.getMockVariableName())
                .append(" = ")
                .append(mocked.mockCall())
                .append(";");
            
            p.line(s.toString());
        }
    }

    public Collection<ProgramObject> getProgramObjects() {
        return Collections.singleton((ProgramObject)mocked);
    }
}
