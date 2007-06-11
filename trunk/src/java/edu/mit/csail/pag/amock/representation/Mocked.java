package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;
import java.util.*;

import edu.mit.csail.pag.amock.trace.Hierarchy;
import edu.mit.csail.pag.amock.util.*;

public class Mocked implements OptionallyDeclarable {
    private String fullClassName; // with periods
    // These only get filled in once the whole test is generated.
    private String classSourceName = null;
    private String varBaseName = null;

    private Hierarchy hierarchy;
    private boolean needsDeclaration = true;

    private final Set<String> typeContexts
        = new HashSet<String>();

    public Mocked(String fullClassName,
                  Hierarchy hierarchy) {
        this.fullClassName = fullClassName;
        this.hierarchy = hierarchy;
    }

    public String getClassSourceName() {
        assert classSourceName != null;
        return classSourceName;
    }

    public String getMockVariableName() {
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

    public boolean needsDeclaration() {
        return needsDeclaration;
    }

    public void doesNotNeedDeclaration() {
        needsDeclaration = false;
    }

    public String mockCall() {
        return "mock(" + getClassSourceName() + ".class)";
    }

    @Override public String toString() {
        return "[mock: " + fullClassName + "]";
    }

    public void usedAsType(Type t) {
        typeContexts.add(t.getInternalName());
    }

    public void becomeMostGeneralClass() {
        String slashName = Misc.classNamePeriodsToSlashes(this.fullClassName);
        
        String mgc = hierarchy.getMostGeneralClass(slashName, typeContexts);
        
        this.fullClassName = Misc.classNameSlashesToPeriods(mgc);
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
