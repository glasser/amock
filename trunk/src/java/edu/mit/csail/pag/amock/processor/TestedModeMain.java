package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;


// TESTED MODE inside method call/constructor
// Here, we're either done with the tested call, or we're seeing
// something we need to mock.
public class TestedModeMain extends TestedState {
    private final PrimaryExecution primaryExecution; // null means constructor

    public TestedModeMain(PreCall openingCall,
                          PrimaryExecution primaryExecution,
                          State continuation,
                          boolean primaryBeingConstructedShouldBeDeclared,
                          Processor p) {
        super(openingCall, continuation, p);
        this.primaryExecution = primaryExecution;
        System.err.format("TMM for call %d '%s'\n", openingCall.callId, openingCall.method.name);

        assert (primaryExecution != null && ! this.openingCall.isConstructor())
            ||
            (primaryExecution == null && this.openingCall.isConstructor());

        if (enteredModeAsConstructor()) {
            initializePrimary(primaryBeingConstructedShouldBeDeclared);
        }
    }

    private boolean enteredModeAsConstructor() {
        return this.primaryExecution == null;
    }

    private void processPostConstructor(PostCall p) {
        if (p.callId != openingCall.callId) {
            // It's not the constructor we're paying attention to.
            // So it really ought to be a nested constructor
            // (super() or this()).  (We'd assert this, but the
            // isTopLevelConstructor flag is just on pre-calls.)
                
            // Ignore it.
            return;
        }

        // We're done the constructor that started us in this
        // state; go to the next one.
        setState(continuation);
    }

    private void initializePrimary(boolean primaryBeingConstructedShouldBeDeclared) {
        // Note that it's hypothetically possible for the receiver
        // to be a Primitive if this is String or a boxed
        // primitive we're looking at; but we should never be in a
        // TestedModeMain for this case (because its init should
        // never be marked as isTopLevelConstructor).
        assert openingCall.receiver instanceof Instance;
        ClassName instanceClassName
            = ((Instance) openingCall.receiver).className;

        Primary primary;

        if (primaryBeingConstructedShouldBeDeclared) {
            primary
                = programObjectFactory().addDeclaredPrimary(instanceClassName,
                                                            openingCall.method,
                                                            getProgramObjects(openingCall.args));
        } else {
            primary = programObjectFactory().addInternalPrimary(instanceClassName);
        }

        boundary().setProgramForTrace(openingCall.receiver, primary);
    }

    public void processPostCall(PostCall p) {
        if (p.isConstructor()) {
            processPostConstructor(p);
            return;
        }

        // Processing the end of an ordinary method call.

        // We only care if it's the one that brought us here.
        if (p.callId != openingCall.callId) {
            return;
        }

        TraceObject ret = p.returnValue;

        // TODO: don't use instanceof, use a method
        if (ret instanceof VoidReturnValue) {
            // Do nothing;
        } else {
            primaryExecution.isEqualTo(getProgramObject(ret));
        }

        setState(continuation);
    }
}
