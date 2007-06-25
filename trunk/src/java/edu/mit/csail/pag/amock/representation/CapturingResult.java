package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.util.MultiSet;

public class CapturingResult implements Result {
    private final InternalPrimary ip;
    private final int index;

    public CapturingResult(InternalPrimary ip, int index) {
        this.ip = ip;
        this.index = index;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        ip.resolveNames(cr, vr);
    }

    public void printSource(LinePrinter p) {
        p.line(String.format("%s.capture(%d)",
                             this.ip.getCaptureVariableName(),
                             this.index));
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();

        pos.add(ip);
        return pos;
    }

    public boolean shouldAppear() {
        return ip.needsDeclaration();
    }

}
