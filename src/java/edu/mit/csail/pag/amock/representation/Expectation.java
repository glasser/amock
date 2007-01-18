package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class Expectation implements CodeChunk {
    private final Mocked mocked;
    private final Integer count;
    private final CodeBlock commands = new BasicCodeBlock();
    private final StringBuilder methodCall = new StringBuilder();
    private boolean firstArgument = false;

    public Expectation(Mocked mocked, Integer count) {
        this.mocked = mocked;
        this.count = count;
    }

    public Expectation method(String methodName) {
        methodCall.append(methodName + "(");
        return this;
    }

    public Expectation withNoArguments() {
        methodCall.append(")");
        return this;
    }

    public Expectation returningConsecutively(Mocked... returneds) {
        commands.addChunk(new CodeLine("will(onConsecutiveCalls("));
        
        CodeBlock returnLines = new IndentingCodeBlock();
        commands.addChunk(returnLines);

        for (int i = 0; i < returneds.length; i++) {
            Mocked returned = returneds[i];
            boolean last = (i == returneds.length - 1);
            
            StringBuilder s = new StringBuilder();
            s.append("returnValue(");
            
            if (returned == null) {
                s.append("null");
            } else {
                s.append(returned.getProxyVariableName());
            }

            s.append(")");
            if (!last) {
                s.append(",");
            }

            returnLines.addChunk(new CodeLine(s.toString()));
        }
        
        commands.addChunk(new CodeLine("));"));
        return this;
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
    }
}