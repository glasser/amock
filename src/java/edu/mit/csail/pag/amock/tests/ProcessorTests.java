package edu.mit.csail.pag.amock.tests;

import java.io.*;

import org.jmock.InAnyOrder;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.processor.*;

/**
 * This test suite needs to be run after the subject traces are
 * generated.
 */

public class ProcessorTests extends AmockUnitTestCase {
    public void testBakery() throws FileNotFoundException {
        // XXX hardcode
        Deserializer d = new Deserializer(new FileInputStream("subjects/out/bakery-trace.xml"));

        TestMethodGenerator tmg = mock(TestMethodGenerator.class);
        // XXX hardcode
        String testedClass = "edu/mit/csail/pag/amock/subjects/bakery/CookieMonster";

        new Processor(d, tmg, testedClass).process();
    }
}
