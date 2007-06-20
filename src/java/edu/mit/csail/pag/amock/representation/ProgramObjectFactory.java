package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

public interface ProgramObjectFactory {
    public Mocked addMock(ClassName className);
    public Primary addPrimary(ClassName className,
                              TraceMethod constructor,
                              ProgramObject[] pos,
                              boolean explicit);
    public RecordPrimary addRecordPrimary(ClassName className);
    public IterationPrimary addIterationPrimary(ClassName className);
    public StaticFieldPrimary addStaticFieldPrimary(TraceField f);
    public PrimaryExecution addPrimaryExecution(Primary p,
                                                TraceMethod m,
                                                ProgramObject... arguments);
    public Expectation addExpectation(ExpectationTarget m, Integer count);
    public void tweakState(Mocked receiver,
                           TraceField field,
                           ProgramObject value);
    public void prepareForNewPrimaryExecution();
}
