package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class ResultsClause implements CodeChunk {
    private ProgramObject returnValue;
    private CodeBlock tweaks;
    private String tweakClass = null;

    static private final String TWEAK_STATE_CLASS
        = "edu.mit.csail.pag.amock.jmock.TweakState";

    public void willReturnValue(ProgramObject returnValue) {
        assert this.returnValue == null;
        this.returnValue = returnValue;
    }

    public void tweakStatement(FieldTweak t) {
        if (tweaks == null) {
            tweaks = new IndentingCodeBlock();
        }

        tweaks.addChunk(t);
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (tweaks != null) {
            tweakClass = cr.getSourceName(TWEAK_STATE_CLASS);
        }
        returnValue.resolveNames(cr, vr);
        tweaks.resolveNames(cr, vr);
    }

    public void printSource(LinePrinter p) {
        if (tweaks == null) {
            if (returnValue != null) {
                p.line("will(returnValue(" +
                       returnValue.getSourceRepresentation() + "));");
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
            p.line("}}, returnValue(" + returnValue.getSourceRepresentation()
                   + ")));");
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();

        if (returnValue != null) {
            pos.add(returnValue);
        }

        if (tweaks != null) {
            pos.addAll(tweaks.getProgramObjects());
        }

        return pos;
    }
}
