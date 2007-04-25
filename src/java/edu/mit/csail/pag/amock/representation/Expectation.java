package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.trace.TraceField;

public class Expectation implements CodeChunk {
    private final Mocked mocked;
    private final Integer count;
    private final CodeBlock commands = new BasicCodeBlock();
    private final StringBuilder methodCall = new StringBuilder();
    private final ResultsClause resultsClause = new ResultsClause();

    public Expectation(Mocked mocked, Integer count) {
        this.mocked = mocked;
        this.count = count;
    }

    public Expectation method(String methodName) {
        methodCall.append(methodName + "(");
        return this;
    }

    public Expectation withArguments(ProgramObject... arguments) {
        if (arguments.length == 0) {
            return withNoArguments();
        }

        boolean first = true;
        for (ProgramObject argument : arguments) {
            if (first) {
                first = false;
            } else {
                methodCall.append(", ");
            }

            methodCall.append(argument.getSourceRepresentation());
        }

        methodCall.append(")");
        return this;
    }

    public Expectation withNoArguments() {
        methodCall.append(")");
        return this;
    }

    // XXX not compatible with tweaks yet -- should use resultsClause
    public Expectation returningConsecutively(ProgramObject... returneds) {
        commands.addChunk(new CodeLine("will(onConsecutiveCalls("));
        
        CodeBlock returnLines = new IndentingCodeBlock();
        commands.addChunk(returnLines);

        for (int i = 0; i < returneds.length; i++) {
            ProgramObject returned = returneds[i];
            boolean last = (i == returneds.length - 1);
            
            StringBuilder s = new StringBuilder();
            s.append("returnValue(");
            
            s.append(returned.getSourceRepresentation());

            s.append(")");
            if (!last) {
                s.append(",");
            }

            returnLines.addChunk(new CodeLine(s.toString()));
        }
        
        commands.addChunk(new CodeLine("));"));
        return this;
    }

    public Expectation returning(ProgramObject returned) {
        resultsClause.willReturnValue(returned.getSourceRepresentation());
        return this;
    }

    public Expectation inSequence(String s) {
        commands.addChunk(new CodeLine("inSequence(" + s + ");"));
        return this;
    }

    public void tweaksState(Mocked receiver,
                            TraceField field,
                            ProgramObject value) {
        StringBuilder s = new StringBuilder();
        s.append(receiver.getSourceRepresentation());
        s.append(".");
        s.append(field.name);
        s.append(" = ");
        s.append(value.getSourceRepresentation());
        s.append(";");
        resultsClause.tweakStatement(s.toString());
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
        s.append(methodCall);
        s.append(";");

        p.line(s.toString());
        commands.printSource(p);
        resultsClause.printSource(p);
    }
}
