package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

public class Processor implements TraceProcessor<TraceEvent> {
    private final ClassName testedClass;

    private final BoundaryTranslator boundary;
    
    private final ProgramObjectFactory programObjectFactory;

    private State state;
    {
        setState(new WaitForCreation(this));
    }

    public Processor(ProgramObjectFactory programObjectFactory,
                     ClassName testedClass,
                     Map<Instance, InstanceInfo> instanceInfos,
                     Hierarchy hierarchy) {
        this.programObjectFactory = programObjectFactory;
        this.testedClass = testedClass;

        this.boundary = new HeuristicBoundaryTranslator(programObjectFactory,
                                                        instanceInfos,
                                                        hierarchy);
    }

    public void processEvent(TraceEvent ev) {
        this.state.processEvent(ev);
    }

    public ClassName getTestedClass() {
        return testedClass;
    }

    public BoundaryTranslator boundary() {
        return boundary;
    }

    public ProgramObjectFactory programObjectFactory() {
        return programObjectFactory;
    }

    public void setState(State newState) {
        state = newState;
        System.err.println(newState.getClass().getSimpleName());
        // One can, say, print out newState.getClass().getSimpleName() here
    }

    public ProgramObject getProgramObject(TraceObject t) {
        return boundary.traceToProgram(t, false);
    }

    public ProgramObject getProgramObjectForReturnAction(TraceObject t) {
        return boundary.traceToProgram(t, true);
    }

    public ProgramObject[] getProgramObjects(TraceObject[] tos) {
        ProgramObject[] pos = new ProgramObject[tos.length];
        for (int i = 0; i < tos.length; i++) {
            pos[i] = getProgramObject(tos[i]);
        }
        return pos;
    }

    public static Map<Instance, InstanceInfo> readInstanceInfos(String iiDump)
        throws FileNotFoundException {
        final Map<Instance, InstanceInfo> iis
            = new HashMap<Instance, InstanceInfo>();

        InputStream iiIn = new FileInputStream(iiDump);
        Deserializer<InstanceInfo>  iiDeserializer
            = Deserializer.getDeserializer(iiIn, InstanceInfo.class);

        iiDeserializer.process(new TraceProcessor<InstanceInfo>() {
                public void processEvent(InstanceInfo ii) {
                    iis.put(ii.instance, ii);
                }
            });

        return iis;
    }
        

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 7 && args.length != 8) {
            throw new RuntimeException("usage: Processor trace-file tcg-dump-out inst-info-dump hierarchy-dump test-case-name test-method-name tested-class [output-package]");
        }

        String traceFileName = args[0];
        String tcgDump = args[1];
        String iiDump = args[2];
        String hierDump = args[3];
        String testCaseName = args[4];
        String testMethodName = args[5];
        ClassName testedClass = ClassName.fromDotted(args[6]);

        String outputPackage = args.length == 8
            ? args[7]
            : testedClass.dottedPackageName();

        Hierarchy hierarchy = Hierarchy.createFromFile(hierDump);

        TestCaseGenerator tcg
            = new TestCaseGenerator(testCaseName, outputPackage);
        TestMethodGenerator tmg = new TestMethodGenerator(testMethodName,
                                                          hierarchy,
                                                          true);
        tcg.addChunk(tmg);

        InputStream in = new FileInputStream(traceFileName);
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);

        Map<Instance, InstanceInfo> iis = readInstanceInfos(iiDump);


        Processor p = new Processor(tmg, testedClass, iis, hierarchy);
        d.process(p);

        PrintStream ps = new PrintStream(tcgDump);
        Serializer<TestCaseGenerator> s = Serializer.getSerializer(ps);

        s.write(tcg);
        s.close();
    }
}
