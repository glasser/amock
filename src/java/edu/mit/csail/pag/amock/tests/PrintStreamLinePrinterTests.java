package edu.mit.csail.pag.amock.tests;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.io.*;

import edu.mit.csail.pag.amock.representation.*;

public class PrintStreamLinePrinterTests extends JMock1AmockUnitTestCase {
    public void testPSLinePrinter() {
        // "os" is just for the sake of getting the constructor to
        // work.
        Mock os = mock(OutputStream.class);
        Mock ps = mock(PrintStream.class,
                       new Class[] { OutputStream.class },
                       new Object[] { os.proxy() });

        PrintStreamLinePrinter pslp
            = new PrintStreamLinePrinter((PrintStream) ps.proxy());

        ps.expects(once())
            .method("println")
            .with(eq("print this line"))
            .isVoid();

        pslp.line("print this line");
    }
}
