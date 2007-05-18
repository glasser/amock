package edu.mit.csail.pag.amock.representation;

public class Mocked implements ProgramObject {
    private final String classSourceName;
    private final String varBaseName;

    private boolean needsDeclaration = true;

    public Mocked(String classSourceName, String varBaseName) {
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getMockVariableName() {
        if (needsDeclaration()) {
            return "mock" + varBaseName;
        } else {
            return mockCall();
        }
    }

    // In jMock 1, we needed separate variables for mocks and proxies;
    // not so in jMock 2.
    public String getProxyVariableName() {
        return getMockVariableName();
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        return getProxyVariableName();
    }

    public boolean needsDeclaration() {
        return needsDeclaration;
    }

    public void doesNotNeedDeclaration() {
        needsDeclaration = false;
    }

    public String mockCall() {
        return "mock(" + getClassSourceName() + ".class)";
    }

    @Override public String toString() {
        return "[mock: " + varBaseName + "]";
    }
}
