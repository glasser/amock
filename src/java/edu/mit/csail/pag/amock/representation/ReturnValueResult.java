package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.util.MultiSet;

public class ReturnValueResult implements Result {
    private final ProgramObject returnValue;

    public ReturnValueResult(ProgramObject returnValue) {
        this.returnValue = returnValue;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        returnValue.resolveNames(cr, vr);
    }

    public void printSource(LinePrinter p) {
        p.line(returnValue.getExpectationReturnValueRepresentation());
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();

        pos.add(returnValue);
        return pos;
    }
}
