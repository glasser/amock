package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.util.MultiSet;

public interface Primary extends ProgramObject {
    public String getClassSourceName();
    public String getPrimaryVariableName();
    public String getConstructor();
    public MultiSet<ProgramObject> getProgramObjects();
}
