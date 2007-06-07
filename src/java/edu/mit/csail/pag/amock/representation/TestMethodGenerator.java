package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.Misc;
import edu.mit.csail.pag.amock.util.MultiSet;
import edu.mit.csail.pag.amock.hooks.IterationPrimaryClassInfo;

public class TestMethodGenerator extends IndentingEmptyLineSeparatedCodeBlock
    implements ProgramObjectFactory {
    // The name for this method.  Note that it is *not* the actual
    // name used in the code: if it is, say, "foo", the actual name
    // used will be testFoo; you can get this with getMethodName().
    // (If we change to use JUnit 4, then getMethodName will just
    // return this.)
    private final String methodName;

    private final boolean ordered;

    private final ClassNameResolver resolver;
    private final Map<String, Integer> nextVarNameNumber
        = new HashMap<String, Integer>();

    private final CodeBlock primarySection;
    private final CodeBlock expectationsAndExecutionSection;

    private CodeBlock currentMocksSection;
    private ExpectationsBlock currentExpectationsBlock;

    private Expectation lastExpectation = null;

    public TestMethodGenerator(String methodName, ClassNameResolver resolver) {
        this(methodName, resolver, false);
    }

    public TestMethodGenerator(String methodName,
                               ClassNameResolver resolver,
                               boolean ordered) {
        this.methodName = methodName;
        this.resolver = resolver;
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
        implements Comparator<MockDeclaration> {
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
        String groupBuilderClass =
            this.resolver.getSourceName("org.jmock.Expectations");
        
        this.currentExpectationsBlock
            = new ExpectationsBlock(groupBuilderClass);
        
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

    public Mocked addMock(String className) {
        Mocked m = new Mocked(resolver.getSourceName(className),
                              getVarNameBase(className));

        currentMocksSection.addChunk(new MockDeclaration(m, resolver));

        return m;
    }

    // "explicit" means that we actually have a declaration for it (at
    // the top) as opposed to it being constructed deep inside tested
    // code (and maybe returned from it).
    public Primary addPrimary(String className,
                              ProgramObject[] pos,
                              boolean explicit) {
        String sourceClassName = resolver.getSourceName(className);
        
        Primary p = new ExplicitlyDeclaredPrimary(sourceClassName,
                                                  getVarNameBase(className),
                                                  pos);

        if (explicit) {
            primarySection.addChunk(new PrimaryDeclaration(p));
        }

        return p;
    }

    public RecordPrimary addRecordPrimary(String className) {
        String dotName = Misc.classNameSlashesToPeriods(className);

        RecordPrimary p = new RecordPrimary(className,
                                            resolver.getSourceName(dotName),
                                            getVarNameBase(dotName));

        primarySection.addChunk(new PrimaryDeclaration(p));
        
        return p;
    }

    public IterationPrimary addIterationPrimary(String className,
                                                Hierarchy hierarchy) {
        String dotName = Misc.classNameSlashesToPeriods(className);
        String implementing
            = IterationPrimaryClassInfo.getClassInfo(dotName, hierarchy)
            .getImplementingClass();
        return new IterationPrimary(className,
                                    resolver.getSourceName(implementing),
                                    getVarNameBase(dotName),
                                    hierarchy);
    }

    // shouldn't be called before any addPrimaryExecution, otherwise
    // there won't be an expectations block!
    public Expectation addExpectation(Mocked m, Integer count) {
        Expectation e = new Expectation(m, count, resolver);
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
                                                  arguments,
                                                  resolver);
        this.expectationsAndExecutionSection.addChunk(a);
        return a;
    }
    
    private String getVarNameBase(String className) {
        String shortName = Misc.classNameWithoutPackage(className);

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
