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
        

    public PrimaryExecution isEqualTo(ProgramObject po) {
        willNeedAssertion();

        String isMethod =
            resolver.getStaticMethodName("org.hamcrest.core.Is", "is");

        String whatItIs = "";

        Set<ProgramObject> pos = new HashSet<ProgramObject>();

        if (po instanceof Primitive && ((Primitive) po).value == null) {
            whatItIs =
                resolver.getStaticMethodName("org.hamcrest.core.IsNull",
                                             "nullValue") + "()";
        } else {
            Type returnValueType = Type.getReturnType(method.descriptor);
            if (returnValueType.getSort() == Type.OBJECT) {
                // TODO should only do this if differs from po's type
                whatItIs = "(" +
                    resolver.getSourceName(returnValueType.getClassName())
                    + ") ";
            }

            pos.add(po);
            
            whatItIs += po.getSourceRepresentation();
        }
        
        constraints.addChunk(new CodeLine(isMethod + "(" + whatItIs + ")",
                                          pos));
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

    public Collection<ProgramObject> getProgramObjects() {
        Set<ProgramObject> pos = new HashSet<ProgramObject>();
        pos.add(primary);
        pos.addAll(Arrays.asList(arguments));
        pos.addAll(constraints.getProgramObjects());
    }
}
