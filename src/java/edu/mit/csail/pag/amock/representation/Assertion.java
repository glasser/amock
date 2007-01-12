package edu.mit.csail.pag.amock.representation;

import java.util.*;
import org.jmock.core.Formatting;

public class Assertion implements CodeChunk {
    private final Primary primary;
    private final String methodName;
    private final Mocked[] arguments;
    private final CodeBlock constraints  = new IndentingCodeBlock();

    public Assertion(Primary primary, String methodName, Mocked[] arguments) {
        this.primary = primary;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public Assertion equalsPrimitive(Object primitive) {
        // TODO refactor into utils, handling strings, chars, etc
        constraints.addChunk(new CodeLine("eq(" + primitive + ")"));
        return this;
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();
        
        s.append("assertThat(");
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
