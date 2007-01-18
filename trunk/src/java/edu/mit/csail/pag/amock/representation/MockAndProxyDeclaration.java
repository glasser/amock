package edu.mit.csail.pag.amock.representation;

public class MockAndProxyDeclaration implements CodeChunk {
    private final Mocked mocked;

    public MockAndProxyDeclaration(Mocked mocked, ClassNameResolver resolver) {
        this.mocked = mocked;
    }

    public void printSource(LinePrinter p) {
        p.line(mocked.getClassSourceName() + " "
               + mocked.getMockVariableName() + " = mock("
               + mocked.getClassSourceName() + ".class);");
    }
}
