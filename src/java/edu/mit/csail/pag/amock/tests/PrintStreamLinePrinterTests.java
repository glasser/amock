package edu.mit.csail.pag.amock.tests;

import org.jmock.InAnyOrder;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class PrintStreamLinePrinterTests extends AmockUnitTestCase {
    public void testPSLinePrinter() {
        final PrintStream ps = mock(PrintStream.class);

        PrintStreamLinePrinter pslp = new PrintStreamLinePrinter(ps);

        expects(new InAnyOrder() {{
            one (ps).println("print this line");
        }});
        
        pslp.line("print this line");
    }
}
