package edu.mit.csail.pag.amock.tests;

import java.io.*;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.jmock.Expectations;

import edu.mit.csail.pag.amock.representation.*;

public class TestCaseGeneratorTests extends AmockUnitTestCase {
    private void thePackage(LinePrinter a) {
        lines(a,
              "package edu.mit.csail.pag.amock.subjects.generated;",
              "");
    }

    private void anImport(LinePrinter a, String className) {
        line(a, "import " + className + ";");
    }

    private void imports(final LinePrinter a, final String... imports) {
        Arrays.sort(imports);

        for (String i : imports) {
            anImport(a, i);
        }
    }

    private void classHeader(LinePrinter a,
                                           String className) {
        lines(a,
              "",
              "public class " + className + " extends MockObjectTestCase {");
    }

    private void classFooter(LinePrinter a) {
        line(a, "}");
    }
    
    public void testEmptyTestCaseGenerator() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        final LinePrinter app = mock(LinePrinter.class);

        thePackage(app);
        imports(app, "edu.mit.csail.pag.amock.jmock.MockObjectTestCase");
        classHeader(app, "MyGeneratedTests");
        classFooter(app);

        tcg.printSource(app);
    }

    public void testMockedMethodGenerators() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        final LinePrinter app = mock(LinePrinter.class);
        final CodeChunk cc1 = mock(CodeChunk.class);
        final CodeChunk cc2 = mock(CodeChunk.class);

        thePackage(app);
        imports(app, "edu.mit.csail.pag.amock.jmock.MockObjectTestCase");
        classHeader(app, "MyGeneratedTests");

        checking(new Expectations() {{
            one (cc1).printSource(with(any(LinePrinter.class)));
        }});

        line(app, "  ");

        checking(new Expectations() {{
            one (cc2).printSource(with(any(LinePrinter.class)));
        }});
                  
        classFooter(app);

        tcg.addChunk(cc1);
        tcg.addChunk(cc2);
        
        tcg.printSource(app);
    }

    public void testGetSourceName() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        final LinePrinter app = mock(LinePrinter.class);

        assertThat(tcg.getSourceName("foo.bar.Baz"), is("Baz"));
        assertThat(tcg.getSourceName("foo.Baz"), is("foo.Baz"));
        assertThat(tcg.getSourceName("foo.bar.Baz"), is("Baz"));

        assertThat(tcg.getStaticMethodName("foo.Beep", "f"), is("f"));
        assertThat(tcg.getStaticMethodName("foo.bar.Baz", "f"), is("Baz.f"));
        assertThat(tcg.getStaticMethodName("foo.Quux", "f"), is("foo.Quux.f"));
        
        thePackage(app);
        imports(app,
                "foo.bar.Baz",
                "edu.mit.csail.pag.amock.jmock.MockObjectTestCase",
                "static foo.Beep.f");
        classHeader(app, "MyGeneratedTests");
        classFooter(app);

        tcg.printSource(app);
    }
}
