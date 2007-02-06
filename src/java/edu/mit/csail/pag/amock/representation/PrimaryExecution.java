package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class PrimaryExecution implements CodeChunk {
    private final Primary primary;
    private final String methodName;
    private final ProgramObject[] arguments;
    private final ClassNameResolver resolver;
    private final CodeBlock constraints  = new IndentingCodeBlock();
    private String assertThatName;

    public PrimaryExecution(Primary primary,
                            String methodName,
                            ProgramObject[] arguments,
                            ClassNameResolver resolver) {
        this.primary = primary;
        this.methodName = methodName;
        this.arguments = arguments;
        this.resolver = resolver;
    }

    private void willNeedAssertion() {
        assertThatName =
            resolver.getStaticMethodName("org.hamcrest.MatcherAssert",
                                         "assertThat");
    }

    private boolean needsAssertion() {
        return assertThatName != null;
    }
        

    public PrimaryExecution isEqualTo(ProgramObject po) {
        willNeedAssertion();

        String isMethod =
            resolver.getStaticMethodName("org.hamcrest.core.Is", "is");
        
        constraints.addChunk(new CodeLine(isMethod + "(" +
                                          po.getSourceRepresentation() + ")"));
        return this;
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();

        if (needsAssertion()) {
            s.append(assertThatName);
            s.append("(");
        }
        
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
        s.append(")");

        if (needsAssertion()) {
            s.append(",");
        
            p.line(s.toString());
            constraints.printSource(p);
            p.line(");");
        } else {
            s.append(";");
            p.line(s.toString());
        }
    }
}
