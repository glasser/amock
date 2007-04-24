package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class Processor {
    private final String testedClass;
    private final Deserializer deserializer;

    private final BoundaryTranslator boundary;
    
    private final TestMethodGenerator testMethodGenerator;

    private State state;
    {
        setState(new WaitForCreation());
    }

    public Processor(Deserializer deserializer,
                     TestMethodGenerator testMethodGenerator,
                     String testedClass) {
        this.deserializer = deserializer;
        this.testMethodGenerator = testMethodGenerator;
        this.testedClass = testedClass;

        this.boundary = new SingleObjectBoundaryTranslator(testMethodGenerator);
    }

    public void process() {
        while (true) {
            TraceEvent ev = deserializer.read();

            if (ev == null) {
                break;
            }

            state.process(ev);
        }
    }

    private void setState(State newState) {
        state = newState;
        // One can, say, print out newState.getClass().getSimpleName() here
    }

    private ProgramObject getProgramObject(TraceObject t) {
        return boundary.traceToProgram(t);
    }

    private ProgramObject[] getProgramObjects(TraceObject[] tos) {
        ProgramObject[] pos = new ProgramObject[tos.length];
        for (int i = 0; i < tos.length; i++) {
            pos[i] = getProgramObject(tos[i]);
        }
        return pos;
    }

    private interface State {
        public void process(TraceEvent ev);
    }

    private abstract class PreCallState implements State {
        public void process(TraceEvent ev) {
            if (!(ev instanceof PreCall)) {
                return;
            }

            processPreCall((PreCall) ev);
        }
        abstract public void processPreCall(PreCall p);
    }

    private abstract class PostCallState implements State {
        public void process(TraceEvent ev) {
            if (!(ev instanceof PostCall)) {
                return;
            }

            processPostCall((PostCall) ev);
        }
        abstract public void processPostCall(PostCall p);
    }

    private abstract class CallState implements State {
        public void process(TraceEvent ev) {
            if (ev instanceof PreCall) {
                processPreCall((PreCall) ev);
            } else if (ev instanceof PostCall) {
                processPostCall((PostCall) ev);
            } else if (ev instanceof FieldRead) {
                processFieldRead((FieldRead) ev);
            }
        }
        abstract public void processPreCall(PreCall p);
        abstract public void processPostCall(PostCall p);
        public void processFieldRead(FieldRead fr) {
            // Do nothing, by default.
        }
    }

    // MOCK MODE initial state
    private class WaitForCreation extends PreCallState {
        public void processPreCall(PreCall p) {
            if (!(p.method.declaringClass.equals(testedClass)
                  && p.isConstructor())) {
                return;
            }
            
            setState(new TestedModeMain(p, null, new MockModeWaiting(), true));
        }
    }

    // MOCK MODE idle state
    private class MockModeWaiting extends PreCallState {
        public void processPreCall(PreCall p) {
            if (p.isConstructor()) {
                // We don't care about random things being
                // constructed.
                
                // TODO: this is assuming that only one explicit
                // primary ever gets constructed; this should really
                // be up to the BoundaryTranslator.
                return;
            }
            
            if (!(boundary.isKnownPrimary(p.receiver))) {
                return;
            }
            Primary receiverPrimary = (Primary) getProgramObject(p.receiver);

            PrimaryExecution primaryExecution =
                testMethodGenerator.addPrimaryExecution(receiverPrimary,
                                                        p.method,
                                                        getProgramObjects(p.args));

            setState(new TestedModeMain(p, primaryExecution, new MockModeWaiting(), false));
        }
    }

    // TESTED MODE inside method call/constructor
    // Here, we're either done with the tested call, or we're seeing
    // something we need to mock.
    private class TestedModeMain extends CallState {
        private final PreCall openingCall;
        private final PrimaryExecution primaryExecution; // null means constructor
        private final State continuation;
        private final boolean explicit;

        private TestedModeMain(PreCall openingCall,
                               PrimaryExecution primaryExecution,
                               State continuation,
                               boolean explicit) {
            this.openingCall = openingCall;
            this.primaryExecution = primaryExecution;
            this.continuation = continuation;
            this.explicit = explicit;

            assert (primaryExecution != null && ! openingCall.isConstructor())
                ||
                (primaryExecution == null && openingCall.isConstructor());

            if (enteredModeAsConstructor()) {
                initializePrimary();
            }
        }

        private boolean enteredModeAsConstructor() {
            return primaryExecution == null;
        }

        public void processPreCall(PreCall p) {
            if (p.isConstructor() && p.isTopLevelConstructor) {
                // We'll go into a nested version of this state to
                // deal with this.
                setState(new TestedModeMain(p, null, this, false));
                return;
            }

            if (! boundary.isKnownMocked(p.receiver) ||
                p.isConstructor()) {
                // Ignore, because it's part of the tested code
                // itself, or maybe a non-top-level constructor.
                return;
            }

            setState(new MockModeNested(p, this));
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

        private void initializePrimary() {
            // Note that it's hypothetically possible for the receiver
            // to be a Primitive if this is String or a boxed
            // primitive we're looking at; but we should never be in a
            // TestedModeMain for this case (because its init should
            // never be marked as isTopLevelConstructor).
            assert openingCall.receiver instanceof Instance;
            String instanceClassName
                = ((Instance) openingCall.receiver).className;

            Primary primary
                = testMethodGenerator.addPrimary(instanceClassName,
                                                 getProgramObjects(openingCall.args),
                                                 explicit);

            boundary.setProgramForTrace(openingCall.receiver, primary);
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

        public void processFieldRead(FieldRead fr) {
            // If we observe a read from a mock, it better have the
            // right value!
            if (boundary.isKnownMocked(fr.receiver)) {
                Mocked receiver = (Mocked) getProgramObject(fr.receiver);
                ProgramObject value = getProgramObject(fr.value);
                testMethodGenerator.tweakState(receiver,
                                               fr.field,
                                               value);
            }
        }
    }

    // MOCK MODE inside method call/constructor
    // Here, we're either done with mocked call, or we're seeing
    // something we need to make really happen.
    private class MockModeNested extends CallState {
        private final PreCall openingCall;
        private final State continuation;
        private final Expectation expectation;

        private MockModeNested(PreCall openingCall, State continuation) {
            this.openingCall = openingCall;
            this.continuation = continuation;

            ProgramObject p = getProgramObject(openingCall.receiver);

            assert p instanceof Mocked;
            Mocked m = (Mocked) p;

            this.expectation =
                testMethodGenerator.addExpectation(m, 1)
                .method(openingCall.method.name);

            if (openingCall.args.length == 0) {
                expectation.withNoArguments();
            } else {
                expectation.withArguments(getProgramObjects(openingCall.args));
            }
        }

        public void processPreCall(PreCall p) {
            if (!boundary.isKnownPrimary(p.receiver)) {
                return;
            }

            // TODO: deal with callbacks
        }

        public void processPostCall(PostCall p) {
            if (p.callId != openingCall.callId) {
                // TODO: maybe this was a callback?
                return;
            }

            TraceObject ret = p.returnValue;

            if (ret instanceof VoidReturnValue) {
                // Do nothing.
            } else {
                ProgramObject m = getProgramObject(ret);

                expectation.returning(m);
            }
            
            setState(continuation);
        }
    }

    private class Idle implements State {
        public void process(TraceEvent te) {
        }
    }
        

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 5) {
            throw new RuntimeException("usage: Processor trace-file unit-test test-case-name test-method-name tested-class");
        }

        String traceFileName = args[0];
        String unitTestName = args[1];
        String testCaseName = args[2];
        String testMethodName = args[3];
        String testedClass = args[4];

        TestCaseGenerator tcg = new TestCaseGenerator(testCaseName);
        TestMethodGenerator tmg = new TestMethodGenerator(testMethodName, tcg,
                                                          true);
        tcg.addChunk(tmg);

        InputStream in = new FileInputStream(traceFileName);
        Deserializer d = Deserializer.getDeserializer(in);
        PrintStream ps = new PrintStream(unitTestName);

        Processor p = new Processor(d, tmg, testedClass);

        // XXX THE TRY/FINALLY IS JUST TEMPORARY, TO GET AN IDEA OF
        // HOW FAR IT GETS BEFORE DYING.
        try {
            p.process();
        } finally {
            tcg.printSource(new PrintStreamLinePrinter(ps));
        }
    }
}