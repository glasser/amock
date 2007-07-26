package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

public interface ProgramObjectFactory {
    public Mocked addMock(ClassName className);
    public DeclarablePrimary addDeclaredPrimary(ClassName className,
                                                TraceMethod constructor,
                                                ProgramObject[] pos);
    public InternalPrimary addInternalPrimary(ClassName className);
    public RecordPrimary addRecordPrimary(ClassName className);
    public IterationPrimary addIterationPrimary(ClassName className);
    public StaticFieldPrimary addStaticFieldPrimary(TraceField f);
    public PrimaryExecution addPrimaryExecution(Primary p,
                                                TraceMethod m,
                                                ProgramObject... arguments);
    public PrimaryExecution addPrimaryExecutionToExpectation(Expectation e,
                                                             Primary p,
                                                             TraceMethod m,
                                                             ProgramObject... arguments);
    public Expectation addExpectation(ExpectationTarget m, Integer count);
    public void tweakState(Mocked receiver,
                           TraceField field,
                           ProgramObject value);
}
