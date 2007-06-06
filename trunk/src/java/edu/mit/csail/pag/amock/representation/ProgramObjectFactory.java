package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.trace.*;

public interface ProgramObjectFactory {
    public Mocked addMock(String className);
    public Primary addPrimary(String className,
                              ProgramObject[] pos,
                              boolean explicit);
    public RecordPrimary addRecordPrimary(String className);
    public IterationPrimary addIterationPrimary(String className);
    public PrimaryExecution addPrimaryExecution(Primary p,
                                                TraceMethod m,
                                                ProgramObject... arguments);
    public Expectation addExpectation(Mocked m, Integer count);
    public void tweakState(Mocked receiver,
                           TraceField field,
                           ProgramObject value);
    public void prepareForNewPrimaryExecution();
}
