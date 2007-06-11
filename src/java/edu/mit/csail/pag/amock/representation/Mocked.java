package edu.mit.csail.pag.amock.representation;

import org.objectweb.asm.Type;
import java.util.*;

import edu.mit.csail.pag.amock.trace.Hierarchy;
import edu.mit.csail.pag.amock.util.*;

public class Mocked implements OptionallyDeclarable {
    private String fullClassName; // with slashes
    private String classSourceName;
    private final String varBaseName;
    private Hierarchy hierarchy;

    private boolean needsDeclaration = true;

    private final Set<String> typeContexts
        = new HashSet<String>();

    public Mocked(String fullClassName,
                  String classSourceName,
                  String varBaseName,
                  Hierarchy hierarchy) {
        this.fullClassName = Misc.classNamePeriodsToSlashes(fullClassName);
        this.classSourceName = classSourceName;
        this.varBaseName = varBaseName;
        this.hierarchy = hierarchy;
    }

    public String getClassSourceName() {
        return classSourceName;
    }

    public String getMockVariableName() {
        if (needsDeclaration()) {
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
        return "[mock: " + varBaseName + "]";
    }

    public void usedAsType(Type t) {
        typeContexts.add(t.getInternalName());
    }

    public void becomeMostGeneralClass(ClassNameResolver r) {
        String mgc = hierarchy.getMostGeneralClass(fullClassName, typeContexts);
        
        this.fullClassName = Misc.classNameSlashesToPeriods(mgc);

        this.classSourceName
            = r.getSourceName(this.fullClassName);
    }
}
