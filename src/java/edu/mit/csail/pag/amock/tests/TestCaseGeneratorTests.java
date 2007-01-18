package edu.mit.csail.pag.amock.tests;

import java.io.*;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.jmock.InThisOrder;
import org.jmock.internal.ExpectationGroupBuilder;

import edu.mit.csail.pag.amock.representation.*;

public class TestCaseGeneratorTests extends AmockUnitTestCase {
    private ExpectationGroupBuilder thePackage(LinePrinter a) {
        return lines(a,
                     "package edu.mit.csail.pag.amock.subjects.generated;",
                     "");
    }

    private ExpectationGroupBuilder anImport(LinePrinter a, String className) {
        return line(a, "import " + className + ";");
    }

    private ExpectationGroupBuilder imports(final LinePrinter a, final String... imports) {
        Arrays.sort(imports);

        return new InThisOrder() {{
            for (String i : imports) {
                expects(anImport(a, i));
            }
        }};
    }

    private ExpectationGroupBuilder classHeader(LinePrinter a,
                                                String className) {
        return lines(a,
                     "",
                     "public class " + className
                     + " extends MockObjectTestCase {");
    }

    private ExpectationGroupBuilder classFooter(LinePrinter a) {
        return line(a, "}");
    }
    
    public void testEmptyTestCaseGenerator() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        final LinePrinter app = mock(LinePrinter.class);

        expects(new InThisOrder() {{
            expects(thePackage(app));
            expects(imports(app,
                            "edu.mit.csail.pag.amock.jmock.MockObjectTestCase"));
            expects(classHeader(app, "MyGeneratedTests"));
            expects(classFooter(app));
        }});

        tcg.printSource(app);
    }

    public void testMockedMethodGenerators() {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");
        final LinePrinter app = mock(LinePrinter.class);
        final CodeChunk cc1 = mock(CodeChunk.class);
        final CodeChunk cc2 = mock(CodeChunk.class);

        expects(new InThisOrder() {{
            expects(thePackage(app));
            expects(imports(app,
                            "edu.mit.csail.pag.amock.jmock.MockObjectTestCase"));
            expects(classHeader(app, "MyGeneratedTests"));

            one (cc1).printSource(with(any(LinePrinter.class)));

            expects(line(app, "  "));

            one (cc2).printSource(with(any(LinePrinter.class)));
                  
            expects(classFooter(app));
        }});


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
        
        expects(new InThisOrder() {{
            expects(thePackage(app));
            expects(imports(app,
                            "foo.bar.Baz",
                            "edu.mit.csail.pag.amock.jmock.MockObjectTestCase",
                            "static foo.Beep.f"));
            expects(classHeader(app, "MyGeneratedTests"));
            expects(classFooter(app));
        }});

        tcg.printSource(app);
    }
}
