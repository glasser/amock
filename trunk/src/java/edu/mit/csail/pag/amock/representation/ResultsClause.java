package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.*;

public class ResultsClause implements CodeChunk {
    // XXX this is wrong.  we want to have at most one tweaks and
    // returns, and have returns after random other actions, so use
    // getActions() everywhere which assembles it...
    private List<Result> actions = new ArrayList<Result>();
    private CodeBlock tweaks;
    private String tweakClass = null;

    private static final ClassName TWEAK_STATE_CLASS
        = ClassName.fromDotted("edu.mit.csail.pag.amock.jmock.TweakState");

    public void willReturnValue(ProgramObject returnValue) {
        actions.add(new ReturnValueResult(returnValue));
    }

    public void tweakStatement(FieldTweak t) {
        if (tweaks == null) {
            tweaks = new IndentingCodeBlock();
        }

        tweaks.addChunk(t);
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        for (Result action : actions) {
            action.resolveNames(cr, vr);
        }
        
        if (tweaks != null) {
            tweakClass = cr.getSourceName(TWEAK_STATE_CLASS);
            tweaks.resolveNames(cr, vr);
        }
    }

    public void printSource(LinePrinter p) {
        if (actions.isEmpty()) {
            return;
        }

        if (actions.size() == 1) {
            p.line("will(");
            actions.get(0).printSource(p);
            p.line(");");
            return;
        }

        p.line("will(doAll(");
        boolean first = true;
        for (Result action : actions) {
            if (first == true) {
                first = false;
            } else {
                p.line(",");
            }

            action.printSource(p);
        }
        p.line("));");
        
        if (tweaks == null) {
            if (returnValue != null) {
                p.line("will(returnValue(" +
                       returnValue.getExpectationReturnValueRepresentation()
                       + "));");
            }
            return;
        }
        
        // We have some tweaks!

        if (returnValue == null) {
            p.line("will(new " + tweakClass + "() { public void go() {");
            tweaks.printSource(p);
            p.line("}});");
        } else {
            p.line("will(doAll(new " + tweakClass + "() { public void go() {");
            tweaks.printSource(p);
            p.line("}}, returnValue("
                   + returnValue.getExpectationReturnValueRepresentation()
                   + ")));");
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();

        for (Result action : actions) {
            pos.addAll(action.getProgramObjects());
        }

        if (tweaks != null) {
            pos.addAll(tweaks.getProgramObjects());
        }

        return pos;
    }
}
