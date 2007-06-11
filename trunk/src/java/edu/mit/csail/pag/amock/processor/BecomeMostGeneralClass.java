package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public class BecomeMostGeneralClass {
    public static void main(String args[]) throws FileNotFoundException {
        if (args.length != 2) {
            throw new RuntimeException("usage: BecomeMostGeneralClass tcg-in.xml tcg-out.xml");
        }
        
        String inFileName = args[0];
        String outFileName = args[1];

        Deserializer<TestCaseGenerator> d
            = Deserializer.getDeserializer(new FileInputStream(inFileName),
                                           TestCaseGenerator.class);

        Serializer<TestCaseGenerator> s
            = Serializer.getSerializer(new FileOutputStream(outFileName));

        new BecomeMostGeneralClass(d, s).run();
    }

    private final Deserializer<TestCaseGenerator> in;
    private final Serializer<TestCaseGenerator> out;

    public BecomeMostGeneralClass(Deserializer<TestCaseGenerator> in,
                                  Serializer<TestCaseGenerator> out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        TestCaseGenerator tcg = in.readOne();

        for (TestMethodGenerator tmg : tcg.getTestMethodGenerators()) {
            MultiSet<ProgramObject> pos = tmg.getProgramObjects();
            
            for (ProgramObject po : pos.elementsAsSet()) {
                if (!(po instanceof Mocked)) {
                    continue;
                }
                Mocked mocked = (Mocked) po;

                mocked.becomeMostGeneralClass(tcg);
            }
        }
        
        out.write(tcg);
        out.close();
    }
    
}
