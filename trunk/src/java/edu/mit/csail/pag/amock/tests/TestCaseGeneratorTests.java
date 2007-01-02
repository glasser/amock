package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.builder.BuilderNamespace;

import java.io.*;

import java.util.Arrays;

import edu.mit.csail.pag.amock.representation.*;

public class TestCaseGeneratorTests extends AmockUnitTestCase {
    private void expectPackage(Mock a) {
        expectLines(a,
                    "package edu.mit.csail.pag.amock.subjects.generated;",
                    "");
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
        expectLines(a,
                    "",
                    "public class " + className + " extends MockObjectTestCase {");
    }

    private void expectClassFooter(Mock a) {
        expectClassFooter(a, a, getLastID(a));
    }
    
    private void expectClassFooter(Mock a, BuilderNamespace ns, String id) {
        expectLine(a, "}", ns, id);
    }
    
    public void testEmptyTestCaseGenerator() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(LinePrinter.class);

        expectPackage(app);
        expectImports(app,
                      "edu.mit.csail.pag.amock.jmock.MockObjectTestCase");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.printSource((LinePrinter) app.proxy());
    }

    public void testMockedMethodGenerators() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(LinePrinter.class);
        Mock cc1 = mock(CodeChunk.class);
        Mock cc2 = mock(CodeChunk.class);
        LinePrinter proxyApp = (LinePrinter) app.proxy();

        expectPackage(app);
        expectImports(app,
                      "edu.mit.csail.pag.amock.jmock.MockObjectTestCase");
        expectClassHeader(app, "MyGeneratedTests");

        tcg.addChunk((CodeChunk) cc1.proxy());
        tcg.addChunk((CodeChunk) cc2.proxy());

        cc1.expects(once())
            .method("printSource").with(same(proxyApp))
            .after(app, getLastID(app))
            .id("called PS");

        expectLine(app, "", cc1, "called PS");

        cc2.expects(once())
            .method("printSource").with(same(proxyApp))
            .after(app, getLastID(app))
            .id("next PS");

        expectClassFooter(app, cc2, "next PS");
        
        tcg.printSource(proxyApp);
    }

    public void testGetSourceName() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        Mock app = mock(LinePrinter.class);

        assertThat(tcg.getSourceName("foo.bar.Baz"), eq("Baz"));
        assertThat(tcg.getSourceName("foo.Baz"), eq("foo.Baz"));
        assertThat(tcg.getSourceName("foo.bar.Baz"), eq("Baz"));

        expectPackage(app);
        expectImports(app,
                      "foo.bar.Baz",
                      "edu.mit.csail.pag.amock.jmock.MockObjectTestCase");
        expectClassHeader(app, "MyGeneratedTests");
        expectClassFooter(app);

        tcg.printSource((LinePrinter) app.proxy());
    }
}
