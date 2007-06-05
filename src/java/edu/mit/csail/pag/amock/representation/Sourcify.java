package edu.mit.csail.pag.amock.representation;

import java.io.*;
import edu.mit.csail.pag.amock.trace.Deserializer;

public class Sourcify {
    public static void main(String args[]) throws FileNotFoundException {
        if (args.length != 2) {
            throw new RuntimeException("usage: Sourcify tcg-dump.xml UnitTest.java");
        }
        
        String tcgDumpFileName = args[0];
        String unitTestFileName = args[1];

        // We make two passes, so we need two deserializers.
        Deserializer<TestCaseGenerator> d
            = Deserializer.getDeserializer(new FileInputStream(tcgDumpFileName),
                                           TestCaseGenerator.class);

        TestCaseGenerator tcg = d.readOne();

        PrintStream out = new PrintStream(unitTestFileName);

        tcg.printSource(new PrintStreamLinePrinter(out));
    }
}
