package edu.mit.csail.pag.amock.representation;

import java.io.PrintStream;

public class TestClassGenerator {
    private final String testClassName;
    
    public TestClassGenerator(String testClassName) {
        this.testClassName = testClassName;
    }

    public void printSource(PrintStream ps) {
        printHeader(ps);
        printFooter(ps);
    }

    private void printHeader(PrintStream ps) {
        ps.println("package edu.mit.csail.pag.subjects.generated;");
        ps.println();
        ps.println("import org.jmock.MockObjectTestCase;");
        ps.println("import org.jmock.Mock;");
        ps.println();
        ps.println("public class " + testClassName +
                   " extends MockObjectTestCase {");
    }

    private void printFooter(PrintStream ps) {
        ps.println("}");
    }
}