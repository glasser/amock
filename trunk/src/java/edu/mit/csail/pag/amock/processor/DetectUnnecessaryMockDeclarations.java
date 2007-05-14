package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

public class DetectUnnecessaryMockDeclarations {
    public static void main(String args[]) throws FileNotFoundException {
        if (args.length != 2) {
            throw new RuntimeException("usage: DetectUnnecessaryMockDeclarations tcg-in.xml tcg-out.xml");
        }
        
        String inFileName = args[0];
        String outFileName = args[1];

        Deserializer<TestCaseGenerator> d
            = Deserializer.getDeserializer(new FileInputStream(inFileName),
                                           TestCaseGenerator.class);

        Serializer<TestCaseGenerator> s
            = Serializer.getSerializer(new FileOutputStream(outFileName));

        new DetectUnnecessaryMockDeclarations(d, s).run();
    }

    private final Deserializer<TestCaseGenerator> in;
    private final Serializer<TestCaseGenerator> out;

    public ConstructorFixer(Deserializer<TestCaseGenerator> in,
                            Serializer<TestCaseGenerator> out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        TestCaseGenerator tcg = in.read();

        // NEXT: actually do the detection (requires refactoring
        // again to multiset)
        
        out.write(tcg);
        out.close();
    }
    
}