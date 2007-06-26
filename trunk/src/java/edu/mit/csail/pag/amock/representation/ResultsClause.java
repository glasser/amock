package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.*;

public class ResultsClause implements CodeChunk {
    // ReturnValueResult must be last!
    private ReturnValueResult returnValueResult;
    // We only have one TweakResult and one CallbackResult, no matter
    // how many individual tweaks there are in it.
    private CallbackResult callbackResult;
    private TweakResult tweakResult;
    private final List<Result> otherResults = new ArrayList<Result>();

    private List<Result> actions() {
        List<Result> actions = new ArrayList<Result>();

        for (Result action : otherResults) {
            if (action.shouldAppear()) {
                actions.add(action);
            }
        }

        if (this.callbackResult != null && this.callbackResult.shouldAppear()) {
            actions.add(this.callbackResult);
        }
        
        if (this.tweakResult != null && this.tweakResult.shouldAppear()) {
            actions.add(this.tweakResult);
        }
        // Return value must be last!
        if (this.returnValueResult != null
            && this.returnValueResult.shouldAppear()) {
            actions.add(this.returnValueResult);
        }
        return actions;
    }

    public void willReturnValue(ProgramObject returnValue) {
        assert this.returnValueResult == null;
        this.returnValueResult = new ReturnValueResult(returnValue);;
        returnValue.getsReturnedFromExpectation();
    }

    public void callsBack(PrimaryExecution pe) {
        if (this.callbackResult == null) {
            this.callbackResult = new CallbackResult();
        }

        this.callbackResult.addPrimaryExecution(pe);
    }

    public void tweakStatement(FieldTweak t) {
        if (this.tweakResult == null) {
            this.tweakResult = new TweakResult();
        }

        this.tweakResult.addTweak(t);
    }

    public void addArgumentCapture(InternalPrimary ip,
                                   int index) {
        this.otherResults.add(new CapturingResult(ip, index));
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        for (Result action : actions()) {
            action.resolveNames(cr, vr);
        }
    }

    public void printSource(LinePrinter p) {
        List<Result> actions = actions();
        
        if (actions.isEmpty()) {
            return;
        }

        final List<StringBuilder> lines = new ArrayList<StringBuilder>();
        LinePrinter collector = new LinePrinter() {
                public void line(String s) {
                    lines.add(new StringBuilder(s));
                }
            };

        boolean needsDoAll = actions.size() > 1;

        boolean first = true;
        for (Result action : actions) {
            if (first) {
                first = false;
            } else {
                lines.get(lines.size() - 1).append(",");
            }
            action.printSource(collector);
        }

        lines.get(0).insert(0, needsDoAll ? "will(doAll(" : "will(");
        lines.get(lines.size() - 1).append(needsDoAll ? "));" : ");");

        // Indent all but first line.
        for (int i = 1; i < lines.size(); i++) {
            lines.get(i).insert(0, "  ");
        }

        for (StringBuilder sb : lines) {
            p.line(sb.toString());
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();

        for (Result action : actions()) {
            pos.addAll(action.getProgramObjects());
        }

        return pos;
    }
}
