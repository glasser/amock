package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import java.util.Arrays;

import edu.mit.csail.pag.amock.representation.*;

public class TestCaseGeneratorTests extends AmockUnitTestCase {
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
    
    public void testEmptyTestCaseGenerator() throws IOException {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(LinePrinter.class);

        expectPackage(app);
        expectImports(app,
                      "org.jmock.MockObjectTestCase",
                      "org.jmock.Mock");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.printSource((LinePrinter) app.proxy());
    }

    public void testMockedMethodGenerators() throws IOException {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(LinePrinter.class);
        Mock cc1 = mock(CodeChunk.class);
        Mock cc2 = mock(CodeChunk.class);
        LinePrinter proxyApp = (LinePrinter) app.proxy();

        expectPackage(app);
        expectImports(app,
                      "org.jmock.MockObjectTestCase",
                      "org.jmock.Mock");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.addCodeChunk((CodeChunk) cc1.proxy());
        tcg.addCodeChunk((CodeChunk) cc2.proxy());

        cc1.expects(once())
            .method("printSource").with(same(proxyApp)).isVoid()
            .id("called PS");

        expectLine(app, "");

        cc2.expects(once())
            .method("printSource").with(same(proxyApp))
            .after(cc1, "called PS")
            .isVoid();
        
        tcg.printSource(proxyApp);
    }

    public void testGetSourceName() throws IOException {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(LinePrinter.class);

        assertThat(tcg.getSourceName("foo.bar.Baz"), eq("Baz"));
        assertThat(tcg.getSourceName("foo.Baz"), eq("foo.Baz"));
        assertThat(tcg.getSourceName("foo.bar.Baz"), eq("Baz"));

        assertThat(tcg.getSourceName("foo.Mock"), eq("foo.Mock"));

        expectPackage(app);
        expectImports(app,
                      "foo.bar.Baz",
                      "org.jmock.MockObjectTestCase",
                      "org.jmock.Mock");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.printSource((LinePrinter) app.proxy());
    }
}
