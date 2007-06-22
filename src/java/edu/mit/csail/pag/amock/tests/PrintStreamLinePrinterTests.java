package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.jmock.Expectations;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class PrintStreamLinePrinterTests extends AmockUnitTestCase {
    public void testPSLinePrinter() {
        final PrintStream ps = mock(PrintStream.class);

        PrintStreamLinePrinter pslp = new PrintStreamLinePrinter(ps);

        checking(new Expectations() {{
            one (ps).println("print this line");
        }});
        
        pslp.line("print this line");
    }
}
