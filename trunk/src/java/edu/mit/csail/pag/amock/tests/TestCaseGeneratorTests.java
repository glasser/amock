package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Arrays;

import edu.mit.lcs.pag.textutil.ReportChecker;

import edu.mit.csail.pag.amock.representation.TestCaseGenerator;

public class TestCaseGeneratorTests extends MockObjectTestCase {
    private void expectLine(Mock app, String s) {
        app.expects(once())
            .method("append")
            .with(eq(s + "\n"))
            .isVoid();
    }
    
    private void expectPackage(Mock a) {
        expectLine(a, "package edu.mit.csail.pag.amock.subjects.generated;");
        expectLine(a, "");
    }

    private void expectImport(Mock a, String className) {
        expectLine(a, "import " + className + ";");
    }

    private void expectImports(Mock a, String... imports) {
        Arrays.sort(imports);
        
        for (String i : imports) {
            expectImport(a, i);
        }
    }

    private void expectClassHeader(Mock a, String className) {
        expectLine(a, "");
        expectLine(a, "public class " + className + " extends MockObjectTestCase {");
    }

    private void expectClassFooter(Mock a) {
        expectLine(a, "}");
    }
    
    public void testEmptyTestCaseGenerator() throws Exception {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(Appendable.class);

        expectPackage(app);
        expectImports(app,
                      "org.jmock.MockObjectTestCase",
                      "org.jmock.Mock");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.printSource((Appendable) app.proxy());
    }
}
