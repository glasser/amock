package edu.mit.csail.pag.amock.representation;


import java.util.*;

import org.objectweb.asm.Type;

public class TestCaseGenerator extends EmptyLineSeparatedCodeBlock
                               implements ClassNameResolver {
    static private final String TEST_CASE_CLASS
        = "edu.mit.csail.pag.amock.jmock.MockObjectTestCase";

    private final String testCaseName;

    private final Map<String, String> importedClasses
        = new HashMap<String, String>();

    public TestCaseGenerator(String testCaseName) {
        this.testCaseName = testCaseName;

        // We'll need this in the "extends" clause, so make sure that
        // it gets imported.
        getSourceName(TEST_CASE_CLASS);
    }

    public void printSource(LinePrinter ps) {
        printHeader(ps);

        super.printSource(ps);

        printFooter(ps);
    }

    private void printHeader(LinePrinter ps) {
        ps.line("package edu.mit.csail.pag.amock.subjects.generated;");
        ps.line("");
        printImports(ps);
        ps.line("");
        ps.line("public class " + testCaseName +
                " extends " + getSourceName(TEST_CASE_CLASS) + " {");
    }

    private void printImports(LinePrinter ps) {
        String[] longNames = importedClasses.values().toArray(new String[0]);
        Arrays.sort(longNames);

        for (String longName : longNames) {
            ps.line("import " + longName + ";");
        }
    }

    private void printFooter(LinePrinter ps) {
        ps.line("}");
    }

    public String getSourceName(String longName) {
        String shortName = Utils.classNameWithoutPackage(longName);
        
        if (importedClasses.containsKey(shortName)) {
            if (importedClasses.get(shortName).equals(longName)) {
                // We've already seen this class.
                return shortName;
            } else {
                // We've seen something that clashes.
                return longName;
            }
        } else {
            // We haven't seen anything like it.
            importedClasses.put(shortName, longName);
            return shortName;
        }
    }
}