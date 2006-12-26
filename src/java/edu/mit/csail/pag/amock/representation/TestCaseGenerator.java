package edu.mit.csail.pag.amock.representation;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import org.objectweb.asm.Type;

public class TestCaseGenerator {
    private final String testCaseName;

    private final Map<String, String> importedClasses
        = new HashMap<String, String>();

    private final List<TestMethodGenerator> methodGenerators
        = new ArrayList<TestMethodGenerator>();

    public TestCaseGenerator(String testCaseName) {
        this.testCaseName = testCaseName;

        getSourceName("org.jmock.Mock");
        getSourceName("org.jmock.MockObjectTestCase");
    }

    public void printSource(Appendable ps) throws IOException {
        printHeader(ps);

        boolean first = true;
        for (TestMethodGenerator tmg : methodGenerators) {
            if (first) {
                first = false;
            } else {
                ps.append("\n");
            }

            tmg.printSource(ps);
        }
        printFooter(ps);
    }

    private void printHeader(Appendable ps) throws IOException {
        ps.append("package edu.mit.csail.pag.amock.subjects.generated;\n");
        ps.append("\n");
        printImports(ps);
        ps.append("\n");
        ps.append("public class " + testCaseName +
                   " extends MockObjectTestCase {\n");
    }

    private void printImports(Appendable ps) throws IOException {
        String[] longNames = importedClasses.values().toArray(new String[0]);
        Arrays.sort(longNames);

        for (String longName : longNames) {
            ps.append("import " + longName + ";\n");
        }
    }

    private void printFooter(Appendable ps) throws IOException {
        ps.append("}\n");
    }

    private static final Pattern LAST_PART
        = Pattern.compile("\\.(\\w+)$");
    /**
     * Given a fully-qualified (with periods) class name, returns a
     * name (possibly qualified) that can be used to refer to it.
     */
    public String getSourceName(String longName) {
        Matcher m = LAST_PART.matcher(longName);

        if (! m.find()) {
            throw new RuntimeException("Weird class name: " + longName);
        }

        String shortName = m.group(1);

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

    public void addMethodGenerator(TestMethodGenerator tmg) {
        methodGenerators.add(tmg);
    }
}