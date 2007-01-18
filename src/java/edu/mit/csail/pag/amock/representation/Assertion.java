package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class Assertion implements CodeChunk {
    private final Primary primary;
    private final String methodName;
    private final Mocked[] arguments;
    private final ClassNameResolver resolver;
    private final CodeBlock constraints  = new IndentingCodeBlock();
    private final String assertThatName;

    public Assertion(Primary primary, String methodName, Mocked[] arguments,
                     ClassNameResolver resolver) {
        this.primary = primary;
        this.methodName = methodName;
        this.arguments = arguments;
        this.resolver = resolver;

        this.assertThatName =
            resolver.getStaticMethodName("org.hamcrest.MatcherAssert", "assertThat");
    }

    public Assertion equalsPrimitive(Object primitive) {
        // TODO refactor into utils, handling strings, chars, etc
        String isMethod =
            resolver.getStaticMethodName("org.hamcrest.core.Is", "is");
        
        constraints.addChunk(new CodeLine(isMethod + "(" + primitive + ")"));
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
        for (Mocked argument : arguments) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            s.append(argument.getProxyVariableName());
        }
        s.append("),");
        
        p.line(s.toString());
        constraints.printSource(p);
        p.line(");");
    }
}
