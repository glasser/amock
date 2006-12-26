package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Arrays;

import edu.mit.lcs.pag.textutil.ReportChecker;

import edu.mit.csail.pag.amock.representation.TestCaseGenerator;

public class TestCaseGeneratorTests extends MockObjectTestCase {
    private void checkPackage(ReportChecker r) throws Exception {
        r.c("package edu.mit.csail.pag.subjects.generated;");
        r.c("");
    }

    private void checkImport(ReportChecker r,
                             String className) throws Exception {
        r.c("import " + className + ";");
    }

    private void checkImports(ReportChecker r,
                              String... imports) throws Exception {
        Arrays.sort(imports);
        
        for (String i : imports) {
            checkImport(r, i);
        }
    }

    private void checkClassHeader(ReportChecker r,
                                  String className) throws Exception {
        r.c("");
        r.c("public class " + className + " extends MockObjectTestCase {");
    }

    private void checkClassFooter(ReportChecker r) throws Exception {
        r.c("}");
        r.done();
    }
    
    public void testEmptyTestCaseGenerator() throws Exception {
        TestCaseGenerator tcg = new TestCaseGenerator("MyGeneratedTests");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        tcg.printSource(new PrintStream(baos));

        ReportChecker r = ReportChecker.create(baos);

        checkPackage(r);
        checkImports(r,
                     "org.jmock.MockObjectTestCase",
                     "org.jmock.Mock");
        checkClassHeader(r, "MyGeneratedTests");
        checkClassFooter(r);
    }
}