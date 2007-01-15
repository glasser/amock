package edu.mit.csail.pag.amock.representation;

public class MockAndProxyDeclaration implements CodeChunk {
    private final Mocked mocked;
    private final String mockClassName;

    static private final String MOCK_CLASS
        = "org.jmock.Mock";

    public MockAndProxyDeclaration(Mocked mocked, ClassNameResolver resolver) {
        this.mocked = mocked;
        this.mockClassName = resolver.getSourceName(MOCK_CLASS);
    }

    public void printSource(LinePrinter p) {
        p.line(mockClassName + " " + mocked.getMockVariableName() + " = mock("
               + mocked.getClassSourceName() + ".class);");
        p.line(mocked.getClassSourceName() + " " + mocked.getProxyVariableName()
               + " = (" + mocked.getClassSourceName() + ") "
               + mocked.getMockVariableName() + ".proxy();");
    }
}
