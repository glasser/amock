package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

// MOCK MODE inside method call/constructor
// Here, we're either done with mocked call, or we're seeing
// something we need to make really happen.
public class MockModeNested extends CallState {
    private final MethodStartEvent openingCall;
    private final State continuation;
    private final Expectation expectation;
    private final StaticTarget staticTarget;

    public MockModeNested(MethodStartEvent openingCall,
                          State continuation,
                          Processor proc) {
        super(proc);
        this.openingCall = openingCall;
        this.continuation = continuation;

        System.err.format("MMN for call %d '%s'\n", openingCall.callId, openingCall.method.name);

        ExpectationTarget target;

        if (openingCall.isStatic()) {
            target = this.staticTarget =
                new StaticTarget(openingCall.method.declaringClass);
        } else {
            ProgramObject p = getProgramObject(openingCall.receiver);
                
            assert p instanceof Mocked;
            target = (Mocked) p;
            this.staticTarget = null;
        }

        if (methodIsUnmockable(openingCall.method)) {
            this.expectation = null;
        } else {
            this.expectation =
                programObjectFactory().addExpectation(target, 1)
                .method(openingCall.method);

            if (openingCall.args.length == 0) {
                expectation.withNoArguments();
            } else {
                expectation.withArguments(getProgramObjects(openingCall.args));
            }
        }
    }

    // jMock won't actually let us mock some methods (see
    // ProxiedObjectIdentity)
    public boolean methodIsUnmockable(TraceMethod m) {
        return ((m.name.equals("hashCode") &&
                 m.descriptor.equals("()I")));
    }

    // For static calls, we really want to make sure that the
    // target is the actual class the method comes from, not a
    // subclass!  (invokestatic operations don't have to name the
    // exact class that the method can be in; it is resolved at
    // runtime!)
    public void processMethodEntry(MethodEntry ev) {
        if (this.staticTarget != null
            && ev.callId == openingCall.callId + 1
            && ev.method.name.equals(openingCall.method.name)
            && ev.method.descriptor.equals(openingCall.method.descriptor)) {
            this.staticTarget.setClassName(ev.method.declaringClass);
        }
    }

    public void processPreCall(PreCall p) {
        if (!boundary().isKnownPrimary(p.receiver)) {
            return;
        }

        // XXX: I just don't think hashCode should be this
        // complicated...
        if (this.expectation == null) {
            return;
        }

        ProgramObject rec = getProgramObject(p.receiver);
        assert rec instanceof Primary;
        Primary primary = (Primary) rec;

        // TODO: deal with callbacks
            
        // We should make sure they happen to explicit and
        // internal primaries but not record primaries.  But huh.
        // Unclear how you actually generate the code to make the
        // callback happen on nested internal primaries.  Grr...

        // Let's start with just explicit and internal primaries.
        if (!(primary instanceof ExplicitlyDeclaredPrimary ||
              primary instanceof InternalPrimary)) {
            System.err.format("Warning: skipping possible callback id=%d method name %s\n",
                              p.callId, p.method.name);
            return;
        }

        PrimaryExecution pe =
            programObjectFactory().addPrimaryExecutionToExpectation(this.expectation,
                                                                    primary,
                                                                    p.method,
                                                                    getProgramObjects(p.args));
        setState(new TestedModeMain(p, pe, this, false, getProcessor()));
    }

    public void processPostCall(PostCall p) {
        processMethodEnd(p);
    }

    @Override public void processMethodExit(MethodExit m) {
        processMethodEnd(m);
    }

    public void processMethodEnd(MethodEndEvent p) {
        if (p.callId != openingCall.callId) {
            // TODO: maybe this was a callback?
            return;
        }

        TraceObject ret = p.returnValue;

        if (ret instanceof VoidReturnValue || this.expectation == null) {
            // Do nothing.
        } else {
            ProgramObject m = getProgramObject(ret);

            expectation.returning(m);
        }
            
        setState(continuation);
    }
}
