package edu.mit.csail.pag.amock.representation;

import java.util.*;
import java.io.Serializable;

import org.objectweb.asm.Type;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;
import edu.mit.csail.pag.amock.hooks.IterationPrimaryClassInfo;

public class TestMethodGenerator extends IndentingEmptyLineSeparatedCodeBlock
    implements ProgramObjectFactory, VariableNameBaseResolver {
    // The name for this method.  Note that it is *not* the actual
    // name used in the code: if it is, say, "foo", the actual name
    // used will be testFoo; you can get this with getMethodName().
    // (If we change to use JUnit 4, then getMethodName will just
    // return this.)
    private final String methodName;

    private final boolean ordered;

    private final Map<String, Integer> nextVarNameNumber
        = new HashMap<String, Integer>();
    private final Hierarchy hierarchy;

    private CodeBlock currentSimpleDeclarationsSection; // mocks, captures
    private ExpectationsBlock currentExpectationsBlock;

    private Expectation lastExpectation = null;

    public TestMethodGenerator(String methodName,
                               Hierarchy hierarchy) {
        this(methodName, hierarchy, false);
    }

    public TestMethodGenerator(String methodName,
                               Hierarchy hierarchy,
                               boolean ordered) {
        this.methodName = methodName;
        this.hierarchy = hierarchy;
        this.ordered = ordered;

        prepareForNewTestedCode();
    }

    private static class SimpleDeclarationComparator
        implements Comparator<SimpleDeclaration>, Serializable {
        public int compare(SimpleDeclaration o1, SimpleDeclaration o2) {
            return o1.getSortKey().compareTo(
                   o2.getSortKey());
        }
    }

    private void createNewSimpleDeclarationsSection() {
        this.currentSimpleDeclarationsSection
            = new SortedCodeBlock<SimpleDeclaration>(new SimpleDeclarationComparator(),
                                                     SimpleDeclaration.class);
        
        addChunk(this.currentSimpleDeclarationsSection);

    }
        
    private void createNewExpectationsSection() {
        this.currentExpectationsBlock = new ExpectationsBlock();
        
        addChunk(this.currentExpectationsBlock);

    }
        
    public void printSource(LinePrinter a) {
        a.line("public void " + getMethodName() + "() throws Throwable {");
        super.printSource(a);
        a.line("}");
    }

    private String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }

        char ch = s.charAt(0);
        
        if (Character.isUpperCase(ch)) {
            return s;
        }

        return String.valueOf(Character.toUpperCase(ch)) + s.substring(1);
    }
    
    public String getMethodName() {
        return "test" + capitalize(methodName);
    }

    public Mocked addMock(ClassName className) {
        Mocked m = new Mocked(className,
                              hierarchy);

        this.currentSimpleDeclarationsSection.addChunk(new MockDeclaration(m));

        return m;
    }

    public DeclarablePrimary addDeclaredPrimary(ClassName className,
                                                TraceMethod constructor,
                                                ProgramObject[] pos) {
        DeclarablePrimary p = new ExplicitlyDeclaredPrimary(className, pos);
        addChunk(new PrimaryDeclaration(p));

        constructor.doUsedAsTypesForArguments(pos);

        return p;
    }

    public InternalPrimary addInternalPrimary(ClassName className) {
        InternalPrimary ip = new InternalPrimary(className, hierarchy);

        this.currentSimpleDeclarationsSection
            .addChunk(new CaptureDeclaration(ip));

        return ip;
    }

    public RecordPrimary addRecordPrimary(ClassName className) {
        RecordPrimary p = new RecordPrimary(className);

        // It's kind of wishful thinking to hope that RecordPrimaries
        // don't rely on each other or make expectations... XXX
        this.currentSimpleDeclarationsSection
            .addChunk(new RecordPrimaryDeclaration(p));
        
        return p;
    }

    public IterationPrimary addIterationPrimary(ClassName className) {
        ClassName implementing
            = IterationPrimaryClassInfo.getClassInfo(className, hierarchy)
            .getImplementingClass();
        return new IterationPrimary(className,
                                    implementing,
                                    hierarchy);
    }

    public StaticFieldPrimary addStaticFieldPrimary(TraceField f) {
        // We don't need to declare it or anything.
        return new StaticFieldPrimary(f);
    }

    public Expectation addExpectation(ExpectationTarget m, Integer count) {
        Expectation e = new Expectation(m, count);
        currentExpectationsBlock.addChunk(e);
        if (ordered) {
            e.inSequence("s");
        }
        this.lastExpectation = e;
        return e;
    }

    // XXX work in progress
    public void tweakState(Mocked receiver,
                           TraceField field,
                           ProgramObject value) {
        // XXX: should be able to include state tweaks before the
        // first expectation
        assert lastExpectation != null;

        lastExpectation.tweaksState(receiver, field, value);
    }

    public void backToMockMode() {
        prepareForNewTestedCode();
    }
    
    private void prepareForNewTestedCode() {
        createNewSimpleDeclarationsSection();
        createNewExpectationsSection();
    }
    
    public PrimaryExecution addPrimaryExecution(Primary p,
                                                TraceMethod m,
                                                ProgramObject... arguments) {
        PrimaryExecution a = makePE(p, m, arguments);
        addChunk(a);
        return a;
    }

    public PrimaryExecution addPrimaryExecutionToExpectation(Expectation e,
                                                             Primary p,
                                                             TraceMethod m,
                                                             ProgramObject... arguments) {
        PrimaryExecution a = makePE(p, m, arguments);
        e.callsBack(a);
        return a;
    }

    private PrimaryExecution makePE(Primary p,
                                    TraceMethod m,
                                    ProgramObject... args) {
        PrimaryExecution a = new PrimaryExecution(p,
                                                  m,
                                                  args);
        m.doUsedAsTypesForArguments(args);
        return a;
    }
        
    
    public String getVarNameBase(ClassName className) {
        String shortName = className.classNameWithoutPackage();

        int num;
        if (nextVarNameNumber.containsKey(shortName)) {
            num = nextVarNameNumber.get(shortName);
            nextVarNameNumber.put(shortName, num+1);
            return shortName+num;
        } else {
            nextVarNameNumber.put(shortName, 1);
            return shortName;
        }
    }
}
