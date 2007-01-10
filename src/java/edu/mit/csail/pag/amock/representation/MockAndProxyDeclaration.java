package edu.mit.csail.pag.amock.representation;

public class MockAndProxyDeclaration implements CodeChunk {
    private final String classSourceName;
    private final String varNameBase;

    public MockAndProxyDeclaration(String classSourceName,
                                   String varNameBase) {
        this.classSourceName = classSourceName;
        this.varNameBase = varNameBase;
    }

    public void printSource(LinePrinter p) {
        p.line("Mock mock" + varNameBase + " = mock(" + classSourceName
               + ".class);");
        p.line(classSourceName + " proxy" + varNameBase + " = ("
               + classSourceName + ") mock" + varNameBase + ".proxy();");
    }
}
