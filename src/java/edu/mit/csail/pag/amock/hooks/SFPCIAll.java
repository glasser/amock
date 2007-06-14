package edu.mit.csail.pag.amock.hooks;

import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

public class SFPCIAll extends StaticFieldPrimaryClassInfo {
    public SFPCIAll(ClassName className) {
        super(className);
    }

    public boolean isSafeStaticField(TraceField f) {
        return true;
    }
}
