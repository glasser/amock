package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.*;

public class CaptureDeclaration implements SimpleDeclaration {
    private final InternalPrimary primary;
    private String captureClassName = null;

    private static final ClassName CAPTURE_CLASS =
        ClassName.fromDotted("edu.mit.csail.pag.amock.jmock.Capture");

    public CaptureDeclaration(InternalPrimary primary) {
        this.primary = primary;
    }

    public void printSource(LinePrinter p) {
        if (primary.needsDeclaration()) {
            p.line(String.format("final %s<%s> %s = capture(%s.class);",
                                 captureClassName,
                                 primary.getClassSourceName(),
                                 primary.getCaptureVariableName(),
                                 primary.getClassSourceName()));
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        return primary.getProgramObjects();
    }

    public String getSortKey() {
        return "NCAPTURE " + primary.getCaptureVariableName();
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        primary.resolveNames(cr, vr);
        if (captureClassName == null) {
            captureClassName = cr.getSourceName(CAPTURE_CLASS);
        }
    }
}
