package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.util.*;

public class CallbackResult implements Result {
    private final CodeBlock callbacks = new IndentingCodeBlock();
    private String callbackClass = null;

    private static final ClassName CALLBACK_CLASS
        = ClassName.fromDotted("edu.mit.csail.pag.amock.jmock.Callback");

    public void addPrimaryExecution(PrimaryExecution pe) {
        callbacks.addChunk(pe);
    }
    
    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        callbackClass = cr.getSourceName(CALLBACK_CLASS);
        callbacks.resolveNames(cr, vr);
    }

    public void printSource(LinePrinter p) {
        p.line("new " + callbackClass + "() { public void go() throws Throwable {");
        callbacks.printSource(p);
        p.line("}}");
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();

        pos.addAll(callbacks.getProgramObjects());
        return pos;
    }

    public boolean shouldAppear() {
        return true;
    }
}
