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

    private final CodeBlock primarySection;
    private final CodeBlock expectationsAndExecutionSection;

    private CodeBlock currentMocksSection;
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

        CodeBlock topMocks = new CommentedCodeBlock("Create mocks.");
        addChunk(topMocks);
        createNewMocksSection(topMocks);

        this.primarySection = new CommentedCodeBlock("Set up primary object.");
        addChunk(this.primarySection);

        this.expectationsAndExecutionSection = new EmptyLineSeparatedCodeBlock();
        addChunk(CommentedCodeBlock.decorating("Set up expectations and run the test.",
                                               this.expectationsAndExecutionSection));
    }

    private static class MockDeclarationComparator
        implements Comparator<MockDeclaration>, Serializable {
        public int compare(MockDeclaration o1, MockDeclaration o2) {
            return o1.getMocked().getMockVariableName().compareTo(
                   o2.getMocked().getMockVariableName());
        }
    }

    private void createNewMocksSection(CodeBlock where) {
        this.currentMocksSection
            = new SortedCodeBlock<MockDeclaration>(new MockDeclarationComparator(),
                                                   MockDeclaration.class);
        
        where.addChunk(this.currentMocksSection);

    }
        
    private void createNewExpectationsSection() {
        this.currentExpectationsBlock = new ExpectationsBlock();
        
        expectationsAndExecutionSection.addChunk(this.currentExpectationsBlock);

    }
        
    public void printSource(LinePrinter a) {
        a.line("public void " + getMethodName() + "() {");
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

        currentMocksSection.addChunk(new MockDeclaration(m));

        return m;
    }

    public DeclarablePrimary addDeclaredPrimary(ClassName className,
                                                TraceMethod constructor,
                                                ProgramObject[] pos) {
        DeclarablePrimary p = new ExplicitlyDeclaredPrimary(className, pos);

        primarySection.addChunk(new PrimaryDeclaration(p));

        constructor.doUsedAsTypesForArguments(pos);

        return p;
    }

    public InternalPrimary addInternalPrimary(ClassName className) {
        return new InternalPrimary(className);
    }

    public RecordPrimary addRecordPrimary(ClassName className) {
        RecordPrimary p = new RecordPrimary(className);

        primarySection.addChunk(new PrimaryDeclaration(p));
        
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

    // shouldn't be called before any addPrimaryExecution, otherwise
    // there won't be an expectations block!
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

    public void prepareForNewPrimaryExecution() {
        createNewMocksSection(this.expectationsAndExecutionSection);
        createNewExpectationsSection();
    }
    
    public PrimaryExecution addPrimaryExecution(Primary p,
                                                TraceMethod m,
                                                ProgramObject... arguments) {
        PrimaryExecution a = new PrimaryExecution(p,
                                                  m,
                                                  arguments);
        this.expectationsAndExecutionSection.addChunk(a);
        m.doUsedAsTypesForArguments(arguments);
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
