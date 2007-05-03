package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.TraceField;

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
        s.append(value.getSourceRepresentation());
        s.append(";");
        p.line(s.toString());
    }

    public Collection<ProgramObject> getProgramObjects() {
        Set<ProgramObject> pos = new HashSet<ProgramObject>();
        pos.add(receiver);
        pos.add(value);
        return Collections.unmodifiableSet(pos);
    }
}
