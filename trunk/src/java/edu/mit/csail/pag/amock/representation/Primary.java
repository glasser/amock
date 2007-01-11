package edu.mit.csail.pag.amock.representation;

public class Primary {
    private final String classSourceName;
    private final String varBaseName;

    public Primary(String classSourceName, String varBaseName) {
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getPrimaryVariableName() {
        return "tested" + varBaseName;
    }

    public String getConstructor() {
        return "new " + getClassSourceName() + "()";
    }
}
