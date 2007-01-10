package edu.mit.csail.pag.amock.representation;

import java.util.*;


public class TestMethodGenerator extends IndentingCodeBlock {
    // The name for this method.  Note that it is *not* the actual
    // name used in the code: if it is, say, "foo", the actual name
    // used will be testFoo; you can get this with getMethodName().
    // (If we change to use JUnit 4, then getMethodName will just
    // return this.)
    private final String methodName;

    private final ClassNameResolver resolver;
    private final Map<String, Integer> nextVarNameNumber
        = new HashMap<String, Integer>();

    private final CodeBlock body;
    private final CodeBlock mocksSection;
    private final CodeBlock expectationsSection;
    private final CodeBlock executionSection;

    public TestMethodGenerator(String methodName, ClassNameResolver resolver) {
        this.methodName = methodName;
        this.resolver = resolver;

        this.body = new EmptyLineSeparatedCodeBlock();
        addChunk(this.body);

        this.mocksSection = new CommentedCodeBlock("Create mocks.");
        body.addChunk(this.mocksSection);

        this.expectationsSection =
            new CommentedCodeBlock("Set up expectations.");
        body.addChunk(this.expectationsSection);

        this.executionSection =
            new CommentedCodeBlock("Run the code under test.");
        body.addChunk(this.executionSection);        
    }
        
    public void printSource(LinePrinter a) {
        a.line("public void " + getMethodName() + " {");
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

    public void addMock(String className) {
        String shortName = resolver.getSourceName(className);

        String varNameBase = getVarNameBase(className);

        mocksSection.addChunk(new MockAndProxyDeclaration(shortName,
                                                          varNameBase));
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
