package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.Misc;

import java.util.*;

public class RecordBoundaryTranslator extends SingleObjectBoundaryTranslator {
    public RecordBoundaryTranslator(TestMethodGenerator testMethodGenerator) {
        super(testMethodGenerator);
    }

    /**
     * This implementation makes a Primary for certain "record type" classes.
     */
    @Override
    protected ProgramObject newProgramObjectForUnknownInstance(Instance i) {
        // XXX generalize
        if (! i.className.equals("java.awt.Rectangle") &&
            ! i.className.equals("java.awt.event.MouseEvent")) {
            return super.newProgramObjectForUnknownInstance(i);
        }

        String className = Misc.classNameSlashesToPeriods(i.className);
        
        return getTestMethodGenerator().addRecordPrimary(className,
                                                         true);
    }
}
