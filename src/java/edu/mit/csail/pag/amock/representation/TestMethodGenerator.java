package edu.mit.csail.pag.amock.representation;

import java.util.*;

import edu.mit.csail.pag.amock.trace.Utils;

public class TestMethodGenerator extends IndentingEmptyLineSeparatedCodeBlock {
    // The name for this method.  Note that it is *not* the actual
    // name used in the code: if it is, say, "foo", the actual name
    // used will be testFoo; you can get this with getMethodName().
    // (If we change to use JUnit 4, then getMethodName will just
    // return this.)
    private final String methodName;

    private final ClassNameResolver resolver;
    private final Map<String, Integer> nextVarNameNumber
        = new HashMap<String, Integer>();

    private final CodeBlock mocksSection;
    private final CodeBlock primarySection;
    private final CodeBlock expectationsSection;
    private final CodeBlock executionSection;

    public TestMethodGenerator(String methodName, ClassNameResolver resolver) {
        this.methodName = methodName;
        this.resolver = resolver;

        this.mocksSection = new CommentedCodeBlock("Create mocks.");
        addChunk(this.mocksSection);

        this.primarySection = new CommentedCodeBlock("Set up primary object.");
        addChunk(this.primarySection);

        CodeBlock expectationsCommentedBlock =
            new CommentedCodeBlock("Set up expectations.");
        addChunk(expectationsCommentedBlock);
        String groupBuilderClass =
            resolver.getSourceName("org.jmock.InAnyOrder");
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

    public Primary addPrimary(String className) {
        Primary p = new Primary(resolver.getSourceName(className),
                                getVarNameBase(className));

        primarySection.addChunk(new PrimaryDeclaration(p));

        return p;
    }

    public Expectation addExpectation(Mocked m, Integer count) {
        Expectation e = new Expectation(m, count);
        expectationsSection.addChunk(e);
        return e;
    }

    public Assertion addAssertion(Primary p, String methodName,
                                  Mocked... arguments) {
        Assertion a = new Assertion(p, methodName, arguments, resolver);
        executionSection.addChunk(a);
        return a;
    }
    
    private String getVarNameBase(String className) {
        String shortName = Utils.classNameWithoutPackage(className);

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
