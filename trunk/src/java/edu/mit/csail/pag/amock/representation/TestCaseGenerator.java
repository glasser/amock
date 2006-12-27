package edu.mit.csail.pag.amock.representation;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import org.objectweb.asm.Type;

public class TestCaseGenerator {
    private final String testCaseName;

    private final Map<String, String> importedClasses
        = new HashMap<String, String>();

    private final List<CodeChunk> codeChunks
        = new ArrayList<CodeChunk>();

    public TestCaseGenerator(String testCaseName) {
        this.testCaseName = testCaseName;

        getSourceName("org.jmock.Mock");
        getSourceName("org.jmock.MockObjectTestCase");
    }

    public void printSource(LinePrinter ps) throws IOException {
        printHeader(ps);

        boolean first = true;
        for (CodeChunk cc : codeChunks) {
            if (first) {
                first = false;
            } else {
                ps.line("");
            }

            cc.printSource(ps);
        }
        printFooter(ps);
    }

    private void printHeader(LinePrinter ps) throws IOException {
        ps.line("package edu.mit.csail.pag.amock.subjects.generated;");
        ps.line("");
        printImports(ps);
        ps.line("");
        ps.line("public class " + testCaseName +
                   " extends MockObjectTestCase {");
    }

    private void printImports(LinePrinter ps) throws IOException {
        String[] longNames = importedClasses.values().toArray(new String[0]);
        Arrays.sort(longNames);

        for (String longName : longNames) {
            ps.line("import " + longName + ";");
        }
    }

    private void printFooter(LinePrinter ps) throws IOException {
        ps.line("}");
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

    public void addCodeChunk(CodeChunk cc) {
        codeChunks.add(cc);
    }
}