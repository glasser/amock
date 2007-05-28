package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class DetectUnnecessaryDeclarations {
    public static void main(String args[]) throws FileNotFoundException {
        if (args.length != 2) {
            throw new RuntimeException("usage: DetectUnnecessaryDeclarations tcg-in.xml tcg-out.xml");
        }
        
        String inFileName = args[0];
        String outFileName = args[1];

        Deserializer<TestCaseGenerator> d
            = Deserializer.getDeserializer(new FileInputStream(inFileName),
                                           TestCaseGenerator.class);

        Serializer<TestCaseGenerator> s
            = Serializer.getSerializer(new FileOutputStream(outFileName));

        new DetectUnnecessaryDeclarations(d, s).run();
    }

    private final Deserializer<TestCaseGenerator> in;
    private final Serializer<TestCaseGenerator> out;

    public DetectUnnecessaryDeclarations(Deserializer<TestCaseGenerator> in,
                                         Serializer<TestCaseGenerator> out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        TestCaseGenerator tcg = in.read();

        for (TestMethodGenerator tmg : tcg.getTestMethodGenerators()) {
            MultiSet<ProgramObject> pos = tmg.getProgramObjects();
            
            for (ProgramObject po : pos.elementsAsSet()) {
                if (!(po instanceof OptionallyDeclarable)) {
                    continue;
                }
                OptionallyDeclarable od = (OptionallyDeclarable) po;

                // Multiplicity 2 means one declaration and one use.
                if (od.needsDeclaration() &&
                    pos.getMultiplicity(od) <= 2) {
                    od.doesNotNeedDeclaration();
                }
            }
        }
        
        out.write(tcg);
        out.close();
    }
    
}