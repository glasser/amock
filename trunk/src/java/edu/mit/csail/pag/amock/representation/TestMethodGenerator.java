package edu.mit.csail.pag.amock.representation;



public class TestMethodGenerator extends IndentingCodeBlock {
    // The name for this method.  Note that it is *not* the actual
    // name used in the code: if it is, say, "foo", the actual name
    // used will be testFoo; you can get this with getSourceName().
    // (If we change to use JUnit 4, then getSourceName will just
    // return this.)
    private final String methodName;

    private final CodeBlock body;
    private final CodeBlock mocksSection;
    private final CodeBlock expectationsSection;
    private final CodeBlock executionSection;
    
    public TestMethodGenerator(String methodName) {
        this.methodName = methodName;

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
        a.line("public void " + getSourceName() + " {");
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
    
    public String getSourceName() {
        return "test" + capitalize(methodName);
    }
}
