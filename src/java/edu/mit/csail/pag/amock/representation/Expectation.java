package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

public class Expectation implements CodeChunk {
    private final ExpectationTarget target;
    private final Integer count;
    private final CodeBlock commands = new BasicCodeBlock();
    private TraceMethod method;
    private List<ProgramObject> methodArguments;
    private final ResultsClause resultsClause;

    public Expectation(ExpectationTarget target,
                       Integer count) {
        this.target = target;
        this.count = count;
        this.resultsClause = new ResultsClause();
    }

    public Expectation method(TraceMethod method) {
        assert this.method == null;
        this.method = method;

        this.target.usedAsType(method.declaringClass.getObjectType());
        
        return this;
    }

    public Expectation withArguments(ProgramObject... arguments) {
        assert this.methodArguments == null;
        this.methodArguments = Arrays.asList(arguments);
        this.method.doUsedAsTypesForArguments(arguments);
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
        returned.usedAsType(Type.getReturnType(this.method.descriptor));
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
        s.append(target.getExpectationTargetName());
        s.append(").");
        appendMethodCall(s);
        s.append(";");

        p.line(s.toString());
        commands.printSource(p);
        resultsClause.printSource(p);
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(target);
        pos.addAll(methodArguments);
        pos.addAll(resultsClause.getProgramObjects());
        return pos;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        target.resolveNames(cr, vr);
        commands.resolveNames(cr, vr);
        for (ProgramObject po : methodArguments) {
            po.resolveNames(cr, vr);
        }
        resultsClause.resolveNames(cr, vr);
    }
}
