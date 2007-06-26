package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import org.objectweb.asm.Type;

/**
 * An InternalPrimary is an object which is created by the code under
 * test.  If we're lucky, it just keeps it to itself, or maybe passes
 * it away once and never does anything with it again; otherwise we'll
 * need fancier tricks like Capture.
 */
public class InternalPrimary extends AbstractProgramObject
    implements Primary, OptionallyDeclarable {
    // Hmm... haven't figured out yet if this should use the "most
    // general type" thing.
    private final ClassName className;
    private String varNameBase;
    private boolean needsDeclarationFlag = true;
    private boolean getsReturnedFromExpectation = false;
    private boolean hasBeenCapturedYet = false;

    private String classSourceName = null;
    
    public InternalPrimary(ClassName className) {
        this.className = className;
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(this);
        return pos;
    }

    public String getPrimaryExecutionReceiverRepresentation() {
        throw new RuntimeException("TODO: pull this out of a capture");
    }

    @Override public boolean expectationArgumentRepresentationIsMatcher() {
        return true;
    }
    
    @Override public String getExpectationArgumentRepresentation(boolean fim,
                                                                 Type t) {
        if (needsDeclaration()) {
            return "with(valueCapturedBy(" + getCaptureVariableName() + "))";
        } else {
            return "with(a(" + this.classSourceName + ".class))";
        }
    }

    @Override public String getExpectationReturnValueRepresentation() {
        assert getsReturnedFromExpectation;
        return "returnValueCapturedBy(" + getCaptureVariableName() + ")";
    }

    @Override public void getsReturnedFromExpectation() {
        getsReturnedFromExpectation = true;
    }

    public boolean hasBeenCapturedYet() {
        return hasBeenCapturedYet;
    }

    public void captureHasBeenCreated() {
        hasBeenCapturedYet = true;
    }

    public String getSourceRepresentation() {
        return "[[[DON'T KNOW HOW TO BE HERE YET]]]";
    }

    public void usedAsType(Type t) {
        // XXX: could do some checking here of the hierarchy
    }

    public boolean needsDeclaration() {
        return getsReturnedFromExpectation || needsDeclarationFlag;
    }

    public void doesNotNeedDeclaration() {
        needsDeclarationFlag = false;
    }

    public int maxUsesForUndeclared() {
        // Multiplicity 3 means one declaration and one use
        // and one capture call...
        return 3;
    }

    public String getCaptureVariableName() {
        assert needsDeclaration();
        assert varNameBase != null;
        return "capturing" + varNameBase;
    }

    // meant to be called from the declaration
    public String getClassSourceName() {
        assert classSourceName != null;
        return classSourceName;
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.classSourceName == null) {
            this.classSourceName = cr.getSourceName(this.className);
            if (needsDeclaration()) {
                this.varNameBase = vr.getVarNameBase(this.className);
            }
        }
    }
}