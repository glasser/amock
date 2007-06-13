package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public interface Primary extends ProgramObject {
    public String getClassSourceName();
    public MultiSet<ProgramObject> getProgramObjects();
}
