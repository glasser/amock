package edu.mit.csail.pag.amock.representation;

import java.util.*;

import org.objectweb.asm.Type;

import edu.mit.csail.pag.amock.trace.Utils;

public class TestCaseGenerator extends IndentingEmptyLineSeparatedCodeBlock
                               implements ClassNameResolver {
    static private final String TEST_CASE_CLASS
        = "edu.mit.csail.pag.amock.jmock.MockObjectTestCase";

    private final String testCaseName;

    private final Map<String, String> importedClasses
        = new HashMap<String, String>();
    private final Map<String, String> importedMethods
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

        String[] methodNames = importedMethods.keySet().toArray(new String[0]);
        Arrays.sort(methodNames);

        for (String methodName : methodNames) {
            ps.line("import static " + importedMethods.get(methodName) + "."
                    + methodName + ";");
        }

    }

    private void printFooter(LinePrinter ps) {
        ps.line("}");
    }

    public String getSourceName(String longName) {
        String shortName = Utils.classNameWithoutPackage(longName);
        
        if (importedClasses.containsKey(shortName)) {
            if (abbreviatingClassNameAs(longName, shortName)) {
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

    private boolean abbreviatingClassNameAs(String longName, String shortName) {
        return importedClasses.containsKey(shortName) &&
            importedClasses.get(shortName).equals(longName);
    }

    public String getStaticMethodName(String className, String methodName) {
        if (importedMethods.containsKey(methodName)) {
            if (importedMethods.get(methodName).equals(className)) {
                // We're already importing this method.
                return methodName;
            } else {
                // We're using this name for something else.  Maybe we
                // have a short class name for it at least?
                String shortName = Utils.classNameWithoutPackage(className);
                if (abbreviatingClassNameAs(className, shortName)) {
                    return shortName + "." + methodName;
                } else {
                    return className + "." + methodName;
                }
            }
        }

        // We haven't seen anything like this yet.
        importedMethods.put(methodName, className);
        return methodName;
    }
}