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
            }
        }
        abstract public void processPreCall(PreCall p);
        abstract public void processPostCall(PostCall p);
    }

    // MOCK MODE initial state
    private class WaitForCreation extends PreCallState {
        public void processPreCall(PreCall p) {
            if (!(p.method.declaringClass.equals(testedClass)
                  && p.isConstructor())) {
                return;
            }
            
            assert p.receiver instanceof ConstructorReceiver;

            setState(new WaitForPrimaryCreationToEnd(p));
        }
    }

    // TESTED MODE inside constructor of explicit primary
    // TODO: merge with TestedModeMain
    private class WaitForPrimaryCreationToEnd extends CallState {
        private final PreCall preCall;

        private WaitForPrimaryCreationToEnd(PreCall preCall) {
            this.preCall = preCall;
        }

        public void processPreCall(PreCall p) {
            if (boundary.isKnownPrimary(p.receiver) ||
                p.isConstructor()) {
                // We're still in TESTED MODE; ignore it.  (We'll
                // remember that a constructed object is Primary when
                // its constructor finishes.)
                return;
            }

            // TODO: deal with nontrivial method calls inside
            // constructor (by making expectations)
            assert false;
        }

        public void processPostCall(PostCall p) {
            if (! p.isConstructor()) {
                // This isn't registering a new object as primary, and
                // it's not what brought us here (because we are in a
                // constructor).
                return;
            }

            assert p.receiver instanceof Instance;
            String instanceClassName = ((Instance) p.receiver).className;

            String constructorClassName =
                Utils.classNameSlashesToPeriods(p.method.declaringClass);

            if (! instanceClassName.equals(constructorClassName)) {
                // It's a superclass constructor.

                // We shouldn't have entered this state at a
                // superclass constructor.
                assert p.callId != preCall.callId;
                return;
            }

            boolean explicit = p.callId == preCall.callId;

            Primary primary = testMethodGenerator.addPrimary(instanceClassName,
                                                             getProgramObjects(p.args),
                                                             explicit);

            boundary.setProgramForTrace(p.receiver, primary);

            if (explicit) {
                setState(new MockModeWaiting());
            }
        }
    }

    // MOCK MODE idle state
    private class MockModeWaiting extends PreCallState {
        public void processPreCall(PreCall p) {
            if (p.receiver instanceof ConstructorReceiver) {
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

            setState(new TestedModeMain(p, primaryExecution));
        }
    }

    // TESTED MODE inside method call/constructor
    // Here, we're either done with the tested call, or we're seeing
    // something we need to mock.
    private class TestedModeMain extends CallState {
        private PreCall openingCall;
        private PrimaryExecution primaryExecution;

        private TestedModeMain(PreCall openingCall,
                               PrimaryExecution primaryExecution) {
            this.openingCall = openingCall;
            this.primaryExecution = primaryExecution;
        }

        public void processPreCall(PreCall p) {
            if (boundary.isKnownPrimary(p.receiver) ||
                p.isConstructor()) {
                // Ignore, because it's part of the tested code
                // itself.  If this is a constructor, we'll catch the
                // constructed object when it's done.
                return;
            }

            setState(new MockModeNested(p, this));
        }

        private void processPostConstructor(PostCall p) {
            // XXX copied code from WaitForPrimaryCreationToEnd

            assert p.receiver instanceof Instance;
            String instanceClassName = ((Instance) p.receiver).className;

            String constructorClassName =
                Utils.classNameSlashesToPeriods(p.method.declaringClass);

            if (! instanceClassName.equals(constructorClassName)) {
                // It's a superclass constructor.
                // Ignore it.
                return;
            }

            // We assume that we are deep in tested code, so this is
            // an implicit primary.
            Primary primary = testMethodGenerator.addPrimary(instanceClassName,
                                                             getProgramObjects(p.args),
                                                             false);

            boundary.setProgramForTrace(p.receiver, primary);
        }

        public void processPostCall(PostCall p) {
            if (p.callId != openingCall.callId) {
                // TODO: assumes that we're currently in a method, not
                // a constructor (but these should probably be merged)
                if (p.isConstructor()) {
                    processPostConstructor(p);
                }
                return;
            }

            TraceObject ret = p.returnValue;

            // TODO: don't use instanceof, use a method
            if (ret instanceof VoidReturnValue) {
                // Do nothing;
            } else {
                primaryExecution.isEqualTo(getProgramObject(ret));
            }

            setState(new MockModeWaiting());
        }
    }

    // MOCK MODE inside method call/constructor
    // Here, we're either done with mocked call, or we're seeing
    // something we need to make really happen.
    private class MockModeNested extends CallState {
        private final PreCall openingCall;
        private final State parentState;
        private final Expectation expectation;

        private MockModeNested(PreCall openingCall, State parentState) {
            this.openingCall = openingCall;
            this.parentState = parentState;

            ProgramObject p = getProgramObject(openingCall.receiver);
            assert openingCall.args.length == 0: new com.thoughtworks.xstream.XStream().toXML(openingCall);
            assert p instanceof Mocked;
            Mocked m = (Mocked) p;

            this.expectation =
                testMethodGenerator.addExpectation(m, 1)
                .method(openingCall.method.name)
                .withNoArguments();
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
            
            setState(parentState);
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
        p.process();

        tcg.printSource(new PrintStreamLinePrinter(ps));
    }
}