package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class PrimaryExecution implements CodeChunk {
    private final Primary primary;
    private final String methodName;
    private final ProgramObject[] arguments;
    private final ClassNameResolver resolver;
    private final CodeBlock constraints  = new IndentingCodeBlock();
    private final String assertThatName;

    public PrimaryExecution(Primary primary,
                            String methodName,
                            ProgramObject[] arguments,
                            ClassNameResolver resolver) {
        this.primary = primary;
        this.methodName = methodName;
        this.arguments = arguments;
        this.resolver = resolver;

        this.assertThatName =
            resolver.getStaticMethodName("org.hamcrest.MatcherAssert",
                                         "assertThat");
    }

    public PrimaryExecution isEqualTo(ProgramObject po) {
        // TODO refactor into utils, handling strings, chars, etc
        String isMethod =
            resolver.getStaticMethodName("org.hamcrest.core.Is", "is");
        
        constraints.addChunk(new CodeLine(isMethod + "(" +
                                          po.getSourceRepresentation() + ")"));
        return this;
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();

        s.append(assertThatName);
        s.append("(");
        s.append(primary.getPrimaryVariableName());
        s.append(".");
        s.append(methodName);
        s.append("(");

        boolean first = true;
        for (ProgramObject argument : arguments) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            s.append(argument.getSourceRepresentation());
        }
        s.append("),");
        
        p.line(s.toString());
        constraints.printSource(p);
        p.line(");");
    }
}
