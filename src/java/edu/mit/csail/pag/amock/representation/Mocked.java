package edu.mit.csail.pag.amock.representation;

public class Mocked {
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

    public String getProxyVariableName() {
        return "proxy" + varBaseName;
    }
}
