package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import java.util.Arrays;

import edu.mit.csail.pag.amock.representation.*;

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
    
    public void testEmptyTestCaseGenerator() throws IOException {
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

    public void testMockedMethodGenerators() throws IOException {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(Appendable.class);
        Mock mg1 = mock(TestMethodGenerator.class);
        Mock mg2 = mock(TestMethodGenerator.class);
        Appendable proxyApp = (Appendable) app.proxy();

        expectPackage(app);
        expectImports(app,
                      "org.jmock.MockObjectTestCase",
                      "org.jmock.Mock");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.addMethodGenerator((TestMethodGenerator) mg1.proxy());
        tcg.addMethodGenerator((TestMethodGenerator) mg2.proxy());

        mg1.expects(once())
            .method("printSource").with(same(proxyApp)).isVoid()
            .id("called PS");

        expectLine(app, "");

        mg2.expects(once())
            .method("printSource").with(same(proxyApp))
            .after(mg1, "called PS")
            .isVoid();
        
        tcg.printSource(proxyApp);
    }

    public void testGetSourceName() throws IOException {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(Appendable.class);

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

        tcg.printSource((Appendable) app.proxy());
    }
}
