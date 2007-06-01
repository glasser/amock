package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.processor.*;

import java.io.*;
import java.util.*;

public abstract class ProcessorTestCase extends AmockUnitTestCase {
    protected void process(String testedClass,
                           TestMethodGenerator tmg)
        throws FileNotFoundException {
        
        InputStream in =
            new FileInputStream(System.getenv("AMOCK_TRACE_FILE"));
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);
        String iiDump = System.getenv("AMOCK_INSTINFO_FILE");
        Map<Instance, InstanceInfo> iis =
            Processor.readInstanceInfos(iiDump);

        new Processor(d, tmg, amockClassSlashes(testedClass), iis).process();
    }
}
