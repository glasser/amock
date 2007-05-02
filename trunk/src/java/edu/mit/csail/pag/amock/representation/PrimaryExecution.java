package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;

import org.objectweb.asm.Type;

public class PrimaryExecution implements CodeChunk {
    private final Primary primary;
    private final TraceMethod method;
    private final ProgramObject[] arguments;
    private final ClassNameResolver resolver;
    private final CodeBlock constraints  = new IndentingCodeBlock();
    private String assertThatName;

    public PrimaryExecution(Primary primary,
                            TraceMethod method,
                            ProgramObject[] arguments,
                            ClassNameResolver resolver) {
        this.primary = primary;
        this.method = method;
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
        

    public PrimaryExecution isEqualTo(final ProgramObject po) {
        willNeedAssertion();

        po.incrementReferenceCount();

        final String isMethod =
            resolver.getStaticMethodName("org.hamcrest.core.Is", "is");

        if (po instanceof Primitive && ((Primitive) po).value == null) {
            String nullness =
                resolver.getStaticMethodName("org.hamcrest.core.IsNull",
                                             "nullValue") + "()";
            constraints.addChunk(new CodeLine(isMethod + "(" + nullness + ")"));

        } else {
            Type returnValueType = Type.getReturnType(method.descriptor);
            final String maybeCast;
            if (returnValueType.getSort() == Type.OBJECT) {
                // TODO should only do this if differs from po's type
                maybeCast = "(" +
                    resolver.getSourceName(returnValueType.getClassName())
                    + ") ";
            } else {
                maybeCast = "";
            }

            constraints.addChunk(new CodeChunk() {
                    public void printSource(LinePrinter p) {
                        p.line(isMethod + "(" + maybeCast +
                               po.getSourceRepresentation() + ")");
                    }
                });
        }

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
        s.append(method.name);
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
