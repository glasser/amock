package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class TestMethodGeneratorTests extends MockObjectTestCase {
    private void expectLine(Mock app, String s) {
        app.expects(once())
            .method("append")
            .with(eq(s + "\n"))
            .isVoid();
    }
    
    public void testEmptyMethodGenerator() throws IOException {
        TestMethodGenerator tmg = new TestMethodGenerator("fooAndBar");
        Mock app = mock(Appendable.class);

        expectLine(app, "public void testFooAndBar {");
        expectLine(app, "}");

        tmg.printSource((Appendable) app.proxy());
    }
}
