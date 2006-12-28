package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class TestMethodGeneratorTests extends AmockUnitTestCase {
    public void testEmptyMethodGenerator() {
        TestMethodGenerator tmg = new TestMethodGenerator("fooAndBar");
        Mock app = mock(LinePrinter.class);

        expectLines(app,
                    "public void testFooAndBar {",
                    "}");

        tmg.printSource((LinePrinter) app.proxy());
    }
}
