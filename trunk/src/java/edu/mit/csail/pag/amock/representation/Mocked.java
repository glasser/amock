package edu.mit.csail.pag.amock.representation;

public class Mocked implements ProgramObject {
    private final String classSourceName;
    private final String varBaseName;

    public Mocked(String classSourceName, String varBaseName) {
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getMockVariableName() {
        return "mock" + varBaseName;
    }

    // In jMock 1, we needed separate variables for mocks and proxies;
    // not so in jMock 2.
    public String getProxyVariableName() {
        return "mock" + varBaseName;
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        return getProxyVariableName();
    }
}
