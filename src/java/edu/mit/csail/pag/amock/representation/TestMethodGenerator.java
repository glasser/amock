package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.Misc;
import edu.mit.csail.pag.amock.util.MultiSet;

public class TestMethodGenerator extends IndentingEmptyLineSeparatedCodeBlock {
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

    private final CodeBlock mocksSection;
    private final CodeBlock primarySection;
    private final CodeBlock expectationsSection;
    private final CodeBlock executionSection;

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

        this.mocksSection = new CommentedCodeBlock("Create mocks.");
        addChunk(this.mocksSection);

        this.primarySection = new CommentedCodeBlock("Set up primary object.");
        addChunk(this.primarySection);

        CodeBlock expectationsCommentedBlock =
            new CommentedCodeBlock("Set up expectations.");
        addChunk(expectationsCommentedBlock);
        String groupBuilderClass =
            resolver.getSourceName("org.jmock.Expectations");
        this.expectationsSection = new ExpectationsBlock(groupBuilderClass);
        expectationsCommentedBlock.addChunk(this.expectationsSection);
        
        this.executionSection =
            new CommentedCodeBlock("Run the code under test.");
        addChunk(this.executionSection);        
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

        mocksSection.addChunk(new MockAndProxyDeclaration(m, resolver));

        return m;
    }

    // "explicit" means that we actually have a declaration for it (at
    // the top) as opposed to it being constructed deep inside tested
    // code (and maybe returned from it)
    public Primary addPrimary(String className,
                              ProgramObject[] pos,
                              boolean explicit) {
        Primary p = new Primary(resolver.getSourceName(className),
                                getVarNameBase(className),
                                pos);

        if (explicit) {
            primarySection.addChunk(new PrimaryDeclaration(p));
        }

        return p;
    }

    public Expectation addExpectation(Mocked m, Integer count) {
        Expectation e = new Expectation(m, count, resolver);
        expectationsSection.addChunk(e);
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

    public PrimaryExecution addPrimaryExecution(Primary p,
                                                TraceMethod m,
                                                ProgramObject... arguments) {
        PrimaryExecution a = new PrimaryExecution(p,
                                                  m,
                                                  arguments,
                                                  resolver);
        executionSection.addChunk(a);
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

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        for (CodeChunk c : new CodeChunk[] { mocksSection,
                                             primarySection,
                                             expectationsSection,
                                             executionSection } ) {
            pos.addAll(c.getProgramObjects());
        }
        return pos;
    }
}
