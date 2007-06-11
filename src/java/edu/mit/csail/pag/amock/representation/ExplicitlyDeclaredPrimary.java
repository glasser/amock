package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class ExplicitlyDeclaredPrimary extends AbstractPrimary {
    private final List<ProgramObject> constructorArguments;

    public ExplicitlyDeclaredPrimary(String fullClassName,
                                     ProgramObject[] constructorArguments) {
        super(fullClassName);
        this.constructorArguments = Arrays.asList(constructorArguments);
    }

    protected List<ProgramObject> getConstructorArguments() {
        return constructorArguments;
    }

    public boolean needsDeclaration() {
        return true;
    }

    public void doesNotNeedDeclaration() {
        // pass
    }
}
