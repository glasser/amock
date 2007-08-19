package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;
import java.util.*;

import edu.mit.csail.pag.amock.trace.Hierarchy;
import edu.mit.csail.pag.amock.util.*;

public class Mocked extends AbstractProgramObject
    implements OptionallyDeclarable, ExpectationTarget, Generalizable {
    private ClassName fullClassName;
    // These only get filled in once the whole test is generated.
    private String classSourceName = null;
    private String varBaseName = null;

    private Hierarchy hierarchy;
    private boolean needsDeclaration = true;

    private final Set<ClassName> typeContexts
        = new HashSet<ClassName>();

    public Mocked(ClassName fullClassName,
                  Hierarchy hierarchy) {
        this.fullClassName = fullClassName;
        this.hierarchy = hierarchy;
    }

    public String getClassSourceName() {
        assert classSourceName != null;
        return classSourceName;
    }

    public String getExpectationTargetName() {
        return getMockVariableName();
    }

    public String getMockVariableName() {
        if (fullClassName.isArray()) {
            // SUCH HACK.  XXX XXX TODO
            return "new " + getClassSourceName() + "{}";
        }
        
        if (needsDeclaration()) {
            assert varBaseName != null;
            return "mock" + varBaseName;
        } else {
            return mockCall();
        }
    }

    // In jMock 1, we needed separate variables for mocks and proxies;
    // not so in jMock 2.
    public String getProxyVariableName() {
        return getMockVariableName();
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        return getProxyVariableName();
    }

    public String getFieldTweakLHSRepresentation() {
        return getSourceRepresentation();
    }

    public boolean needsDeclaration() {
        return needsDeclaration;
    }

    public void doesNotNeedDeclaration() {
        needsDeclaration = false;
    }

    public int maxUsesForUndeclared() {
        // Multiplicity 2 means one declaration and one use.
        return 2;
    }

    public String mockCall() {
        return "mock(" + getClassSourceName() + ".class)";
    }

    @Override public String toString() {
        return "[mock: " + fullClassName.dotted() + "]";
    }

    public void usedAsType(Type t) {
        typeContexts.add(ClassName.fromSlashed(t.getInternalName()));
    }

    public void becomeMostGeneralClass() {
        this.fullClassName
            = hierarchy.getMostGeneralClass(this.fullClassName, typeContexts);
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        if (this.classSourceName == null) {
            this.classSourceName = cr.getSourceName(this.fullClassName);
            if (needsDeclaration()) {
                this.varBaseName = vr.getVarNameBase(this.fullClassName);
            }
        }
    }
}
