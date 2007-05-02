package edu.mit.csail.pag.amock.representation;

public class Primary implements ProgramObject {
    private final String classSourceName;
    private final String varBaseName;
    private final ProgramObject[] constructorArguments;

    public Primary(String classSourceName, String varBaseName,
                   ProgramObject[] constructorArguments) {
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
        this.constructorArguments = constructorArguments;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getPrimaryVariableName() {
        return "tested" + varBaseName;
    }

    public String getConstructor() {
        StringBuilder s = new StringBuilder();
        s.append("new ");
        s.append(getClassSourceName());
        s.append("(");

        boolean first = true;

        for (ProgramObject o : constructorArguments) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            s.append(o.getSourceRepresentation());
        }

        s.append(")");
        
        return s.toString();
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        return getPrimaryVariableName();
    }

    // NEXT: getProgramObjects (even though it's not a CodeChunk)
}
