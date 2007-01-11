package edu.mit.csail.pag.amock.representation;

public class MockAndProxyDeclaration implements CodeChunk {
    private final Mocked mocked;

    public MockAndProxyDeclaration(Mocked mocked) {
        this.mocked = mocked;
    }

    public void printSource(LinePrinter p) {
        p.line("Mock " + mocked.getMockVariableName() + " = mock("
               + mocked.getClassSourceName() + ".class);");
        p.line(mocked.getClassSourceName() + " " + mocked.getProxyVariableName()
               + " = (" + mocked.getClassSourceName() + ") "
               + mocked.getMockVariableName() + ".proxy();");
    }
}
