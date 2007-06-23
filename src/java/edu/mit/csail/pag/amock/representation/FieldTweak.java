package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.TraceField;
import edu.mit.csail.pag.amock.util.MultiSet;

public class FieldTweak implements CodeChunk {
    public final Mocked receiver;
    public final TraceField field;
    public final ProgramObject value;

    public FieldTweak(Mocked receiver,
                      TraceField field,
                      ProgramObject value) {
        this.receiver = receiver;
        this.field = field;
        this.value = value;
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();
        s.append(receiver.getSourceRepresentation());
        s.append(".");
        s.append(field.name);
        s.append(" = ");
        s.append(value.getFieldTweakValueRepresentation());
        s.append(";");
        p.line(s.toString());
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(receiver);
        pos.add(value);
        return pos;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        receiver.resolveNames(cr, vr);
        value.resolveNames(cr, vr);
    }
}
