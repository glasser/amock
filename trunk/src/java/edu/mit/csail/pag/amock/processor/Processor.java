package edu.mit.csail.pag.amock.processor;

import java.io.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class Processor {
    private static final String TEST_CASE_NAME = "CookieMonsterTest";
    private static final String TEST_METHOD_NAME = "cookieEating";
    private static final String TESTED_CLASS = "edu/mit/csail/pag/amock/subjects/bakery/CookieMonster";
    
    private TestCaseGenerator testCaseGenerator;
    private TestMethodGenerator testMethodGenerator;
    private final Deserializer deserializer;
    private Primary primary;
    private TraceObject primaryInTrace;

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

    public void print() {
        testCaseGenerator.printSource(new PrintStreamLinePrinter(System.out));
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

    private class WaitForCreation extends PreCallState {
        public void processPreCall(PreCall p) {
            if (!(p.method.declaringClass.equals(TESTED_CLASS)
                  && p.method.name.equals("<init>"))) {
                return;
            }
            
            assert p.receiver instanceof ConstructorReceiver;
            assert p.args.length == 0; // TODO: deal with args

            String classNameWithPeriods =
                Utils.getObjectType(p.method.declaringClass).getClassName();

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
            if (!(p.callId == preCall.callId)) {
                return;
            }

            primaryInTrace = p.receiver;

            setState(new WaitForCallOnPrimary());
        }
    }

    private class WaitForCallOnPrimary extends PreCallState {
        public void processPreCall(PreCall p) {
            // XXX next.
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        Deserializer d = new Deserializer(new FileInputStream(args[0]));

        Processor p = new Processor(d);
        p.process();
        p.print();
    }
}