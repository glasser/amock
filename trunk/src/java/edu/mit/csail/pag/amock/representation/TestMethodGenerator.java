package edu.mit.csail.pag.amock.representation;

import java.io.IOException;

public class TestMethodGenerator implements CodeChunk {
    // The name for this method.  Note that it is *not* the actual
    // name used in the code: if it is, say, "foo", the actual name
    // used will be testFoo; you can get this with getSourceName().
    // (If we change to use JUnit 4, then getSourceName will just
    // return this.)
    private final String methodName;
    
    public TestMethodGenerator(String methodName) {
        this.methodName = methodName;
    }
        
    public void printSource(Appendable a) throws IOException {
        a.append("public void " + getSourceName() + " {\n");
        a.append("}\n");
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
