package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class MockDeclaration implements CodeChunk {
    private final Mocked mocked;

    public MockDeclaration(Mocked mocked, ClassNameResolver resolver) {
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

    public MultiSet<ProgramObject> getProgramObjects() {
        return MultiSet.singleton((ProgramObject)mocked);
    }

    public Mocked getMocked() {
        return mocked;
    }
}
