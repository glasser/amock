package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class Processor {
    private final String testedClass;
    private final Deserializer deserializer;
    
    private final TestMethodGenerator testMethodGenerator;
    private Primary primary;
    private TraceObject primaryInTrace;
    private final Map<TraceObject, Mocked> mockedForTrace =
        new HashMap<TraceObject, Mocked>();

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
        if (mockedForTrace.containsKey(t)) {
            return mockedForTrace.get(t);
        } else if (primaryInTrace != null && primaryInTrace.equals(t)) {
            return primary;
        } else if (t instanceof Primitive) {
            // Primitives are both ProgramObjects and TraceObjects.
            return (Primitive) t;
        } else if (t instanceof Instance) {
            Instance i = (Instance) t; 

            String className = Utils.classNameSlashesToPeriods(i.className);
            Mocked m = testMethodGenerator.addMock(className);

            mockedForTrace.put(t, m);
            return m;
        } else {
            throw new RuntimeException("Unexpected TraceObject: " + t);
        }
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

    private class WaitForCreation extends PreCallState {
        public void processPreCall(PreCall p) {
            if (!(p.method.declaringClass.equals(testedClass)
                  && p.method.name.equals("<init>"))) {
                return;
            }
            
            assert p.receiver instanceof ConstructorReceiver;

            String classNameWithPeriods =
                Utils.classNameSlashesToPeriods(p.method.declaringClass);

            primary = testMethodGenerator.addPrimary(classNameWithPeriods,
                                                     getProgramObjects(p.args));

            // TODO: don't assume that nothing interesting happens
            // during primary instance construction!

            setState(new WaitForPrimaryCreationToEnd(p));
        }
    }

    private class WaitForPrimaryCreationToEnd extends PostCallState {
        private final PreCall preCall;

        private WaitForPrimaryCreationToEnd(PreCall preCall) {
            this.preCall = preCall;
        }

        public void processPostCall(PostCall p) {
            if (p.callId != preCall.callId) {
                return;
            }

            primaryInTrace = p.receiver;

            setState(new WaitForCallOnPrimary());
        }
    }

    private ProgramObject[] getProgramObjects(TraceObject[] tos) {
        ProgramObject[] pos = new ProgramObject[tos.length];
        for (int i = 0; i < tos.length; i++) {
            pos[i] = getProgramObject(tos[i]);
        }
        return pos;
    }

    private class WaitForCallOnPrimary extends PreCallState {
        public void processPreCall(PreCall p) {
            if (!(primaryInTrace.equals(p.receiver))) {
                return;
            }

            Assertion assertion =
                testMethodGenerator.addAssertion(primary, p.method.name,
                                                 getProgramObjects(p.args));

            setState(new InsideTestedCall(p, assertion));
        }
    }

    // Here, we're either done with the tested call, or we're seeing
    // something we need to mock.
    private class InsideTestedCall extends CallState {
        private PreCall openingCall;
        private Assertion assertion;

        private InsideTestedCall(PreCall openingCall, Assertion assertion) {
            this.openingCall = openingCall;
            this.assertion = assertion;
        }

        public void processPreCall(PreCall p) {
            if (primaryInTrace.equals(p.receiver)) {
                // TODO ignore, I think, because it's on the primary itself
                return;
            }

            setState(new InsideMockedCall(p, this));
        }

        public void processPostCall(PostCall p) {
            if (p.callId != openingCall.callId) {
                // XXX here
                return;
            }

            TraceObject ret = p.returnValue;

            // TODO: deal with non-primitive return values
            assert ret instanceof Primitive;
            Primitive prim = (Primitive) ret;

            assertion.equalsPrimitive(prim.value);

            setState(new Idle());
        }
    }

    // Here, we're either done with mocked call, or we're seeing
    // something we need to make really happen.
    private class InsideMockedCall extends CallState {
        private final PreCall openingCall;
        private final State parentState;
        private final Expectation expectation;

        private InsideMockedCall(PreCall openingCall, State parentState) {
            this.openingCall = openingCall;
            this.parentState = parentState;

            ProgramObject p = getProgramObject(openingCall.receiver);
            assert openingCall.args.length == 0;
            assert p instanceof Mocked;
            Mocked m = (Mocked) p;

            this.expectation =
                testMethodGenerator.addExpectation(m, 1)
                .method(openingCall.method.name)
                .withNoArguments();
        }

        public void processPreCall(PreCall p) {
            if (! primaryInTrace.equals(p.receiver)) {
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

            if (ret instanceof Instance || ret instanceof Primitive) {
                ProgramObject m = getProgramObject(ret);

                expectation.returning(m);
            } else if (ret instanceof VoidReturnValue) {
                // Do nothing.
            } else {
                // TODO: deal with other types
                assert false;
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
        TestMethodGenerator tmg = new TestMethodGenerator(testMethodName, tcg);
        tcg.addChunk(tmg);
        
        Deserializer d = new Deserializer(new FileInputStream(traceFileName));
        PrintStream ps = new PrintStream(unitTestName);

        Processor p = new Processor(d, tmg, testedClass);
        p.process();

        tcg.printSource(new PrintStreamLinePrinter(ps));
    }
}