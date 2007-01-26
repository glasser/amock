package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class Processor {
    private static final String TEST_CASE_NAME = "AutoCookieMonsterTest";
    private static final String TEST_METHOD_NAME = "cookieEating";
    private static final String TESTED_CLASS = "edu/mit/csail/pag/amock/subjects/bakery/CookieMonster";
    
    private TestCaseGenerator testCaseGenerator;
    private TestMethodGenerator testMethodGenerator;
    private final Deserializer deserializer;
    private Primary primary;
    private TraceObject primaryInTrace;
    private Map<TraceObject, Mocked> mockedForTrace =
        new HashMap<TraceObject, Mocked>();

    private State state;
    {
        setState(new WaitForCreation());
    }

    public Processor(Deserializer deserializer) {
        this.deserializer = deserializer;

        testCaseGenerator = new TestCaseGenerator(TEST_CASE_NAME);
        testMethodGenerator =
            new TestMethodGenerator(TEST_METHOD_NAME, testCaseGenerator);
        testCaseGenerator.addChunk(testMethodGenerator);
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
        System.err.println("Entering state: "
                           + newState.getClass().getSimpleName());
    }

    public void print(PrintStream ps) {
        testCaseGenerator.printSource(new PrintStreamLinePrinter(ps));
    }

    private Mocked getMocked(TraceObject t) {
        if (mockedForTrace.containsKey(t)) {
            return mockedForTrace.get(t);
        }

        // TODO: deal with primitive arguments
        assert t instanceof Instance;
        Instance i = (Instance) t; 

        String className = Utils.classNameSlashesToPeriods(i.className);
        Mocked m = testMethodGenerator.addMock(className);

        mockedForTrace.put(t, m);
        return m;
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
            if (!(p.method.declaringClass.equals(TESTED_CLASS)
                  && p.method.name.equals("<init>"))) {
                return;
            }
            
            assert p.receiver instanceof ConstructorReceiver;
            assert p.args.length == 0; // TODO: deal with args

            String classNameWithPeriods =
                Utils.classNameSlashesToPeriods(p.method.declaringClass);

            primary = testMethodGenerator.addPrimary(classNameWithPeriods);

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

    private class WaitForCallOnPrimary extends PreCallState {
        public void processPreCall(PreCall p) {
            if (!(primaryInTrace.equals(p.receiver))) {
                return;
            }

            // TODO: args can also be Primaries or primitives.
            Mocked[] arguments = new Mocked[p.args.length];
            for (int i = 0; i < p.args.length; i++) {
                arguments[i] = getMocked(p.args[i]);
            }
            
            Assertion assertion =
                testMethodGenerator.addAssertion(primary, p.method.name,
                                                 arguments);

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

            Mocked m = getMocked(openingCall.receiver);
            assert openingCall.args.length == 0;

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

            // TODO: deal with primitive return values
            if (ret instanceof Instance) {
                Instance i = (Instance) ret;

                Mocked m = getMocked(i);

                expectation.returningConsecutively(m);

                // XXX: actually make the expectation
            } else if (ret instanceof VoidReturnValue) {
                // Do nothing.
            } else if (ret instanceof Primitive) {
                Primitive prim = (Primitive) ret;

                if (prim.value == null) {
                    expectation.returningConsecutively((Mocked)null);
                } else {
                    // TODO: deal with other types
                }
            } else {
                // TODO: deal with other types
            }
            
            setState(parentState);
        }
    }

    private class Idle implements State {
        public void process(TraceEvent te) {
        }
    }
        

    public static void main(String args[]) throws FileNotFoundException {
        Deserializer d = new Deserializer(new FileInputStream(args[0]));
        PrintStream ps = new PrintStream(args[1]);

        Processor p = new Processor(d);
        p.process();
        p.print(ps);
    }
}