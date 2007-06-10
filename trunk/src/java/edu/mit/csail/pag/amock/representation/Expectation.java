package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

public class Expectation implements CodeChunk {
    private final Mocked mocked;
    private final Integer count;
    private final CodeBlock commands = new BasicCodeBlock();
    private TraceMethod method;
    private List<ProgramObject> methodArguments;
    private final ResultsClause resultsClause;

    private final ClassNameResolver resolver;

    public Expectation(Mocked mocked,
                       Integer count,
                       ClassNameResolver resolver) {
        this.mocked = mocked;
        this.count = count;
        this.resolver = resolver;
        this.resultsClause = new ResultsClause(resolver);
    }

    public Expectation method(TraceMethod method) {
        assert this.method == null;
        this.method = method;

        this.mocked.usedAsType(Misc.getObjectType(method.declaringClass));
        
        return this;
    }

    public Expectation withArguments(ProgramObject... arguments) {
        assert this.methodArguments == null;
        this.methodArguments = Arrays.asList(arguments);
        return this;
    }
    
    public Expectation withNoArguments() {
        return withArguments(new ProgramObject[0]);
    }

    private void appendMethodCall(StringBuilder s) {
        s.append(method.name);
        s.append("(");
        
        boolean first = true;
        for (ProgramObject argument : methodArguments) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            s.append(argument.getSourceRepresentation());
        }

        s.append(")");
    }

    public Expectation returning(ProgramObject returned) {
        resultsClause.willReturnValue(returned);
        return this;
    }

    public Expectation inSequence(String s) {
        commands.addChunk(new CodeLine("inSequence(" + s + ");"));
        return this;
    }

    public void tweaksState(Mocked receiver,
                            TraceField field,
                            ProgramObject value) {
        FieldTweak t = new FieldTweak(receiver, field, value);
        resultsClause.tweakStatement(t);
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();
        
        if (count == 1) {
            s.append("one (");
        } else {
            s.append("exactly(" + count + ").of (");
        }
        s.append(mocked.getMockVariableName());
        s.append(").");
        appendMethodCall(s);
        s.append(";");

        p.line(s.toString());
        commands.printSource(p);
        resultsClause.printSource(p);
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(mocked);
        pos.addAll(methodArguments);
        pos.addAll(resultsClause.getProgramObjects());
        return pos;
    }
}
