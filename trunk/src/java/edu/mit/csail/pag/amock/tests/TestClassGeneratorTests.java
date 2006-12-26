package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import edu.mit.lcs.pag.textutil.ReportChecker;

import edu.mit.csail.pag.amock.representation.TestClassGenerator;

public class TestClassGeneratorTests extends MockObjectTestCase {
    public void testEmptyTestClassGenerator() throws Exception {
        TestClassGenerator tcg = new TestClassGenerator("MyGeneratedTests");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        tcg.printSource(new PrintStream(baos));

        ReportChecker r = ReportChecker.create(baos);

        r.c("package edu.mit.csail.pag.subjects.generated;");
        r.c("");
        r.c("import org.jmock.MockObjectTestCase;");
        r.c("import org.jmock.Mock;");
        r.c("");
        r.c("public class MyGeneratedTests extends MockObjectTestCase {");
        r.c("}");
        r.done();
    }
}