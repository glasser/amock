package edu.mit.csail.pag.amock.representation;

import java.util.*;

public class ResultsClause implements CodeChunk {
    private ProgramObject returnValue;
    private CodeBlock tweaks;
    private String tweakClass;
    private final ClassNameResolver resolver;

    static private final String TWEAK_STATE_CLASS
        = "edu.mit.csail.pag.amock.jmock.TweakState";

    public ResultsClause(ClassNameResolver resolver) {
        this.resolver = resolver;
    }
    
    public void willReturnValue(ProgramObject returnValue) {
        assert this.returnValue == null;
        this.returnValue = returnValue;
    }

    public void tweakStatement(FieldTweak t) {
        if (tweaks == null) {
            tweaks = new IndentingCodeBlock();
            tweakClass = resolver.getSourceName(TWEAK_STATE_CLASS);
        }

        tweaks.addChunk(t);
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

    public Collection<ProgramObject> getProgramObjects() {
        Set<ProgramObject> pos = new HashSet<ProgramObject>();

        if (returnValue != null) {
            pos.add(returnValue);
        }

        if (tweaks != null) {
            pos.addAll(tweaks.getProgramObjects());
        }

        return Collections.unmodifiableSet(pos);
    }
}
