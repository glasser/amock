package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class Processor {
    private final String testedClass;
    private final Deserializer<TraceEvent> deserializer;

    private final BoundaryTranslator boundary;
    
    private final ProgramObjectFactory programObjectFactory;

    private State state;
    {
        setState(new WaitForCreation());
    }

    public Processor(Deserializer<TraceEvent> deserializer,
                     ProgramObjectFactory programObjectFactory,
                     String testedClass,
                     Set<Instance> potentialRecordPrimaries) {
        this.deserializer = deserializer;
        this.programObjectFactory = programObjectFactory;
        this.testedClass = testedClass;

        this.boundary = new RecordBoundaryTranslator(programObjectFactory,
                                                     potentialRecordPrimaries);
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
                programObjectFactory.addPrimaryExecution(receiverPrimary,
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

            if (boundary.isKnownPrimary(p.receiver)) {
                ProgramObject rec = getProgramObject(p.receiver);
                if (rec instanceof RecordPrimary) {
                    setState(new RecordPrimaryInvocation(p, this));
                    return;
                }
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
                = programObjectFactory.addPrimary(instanceClassName,
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
                programObjectFactory.tweakState(receiver,
                                                fr.field,
                                                value);
            } else if (boundary.isKnownPrimary(fr.receiver)) {
                ProgramObject recPO = getProgramObject(fr.receiver);
                if (!(recPO instanceof RecordPrimary)) {
                    return;
                }
                RecordPrimary rec = (RecordPrimary) recPO;
                
                ProgramObject value = getProgramObject(fr.value);
                rec.haveFieldValue(fr.field, value);
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
                programObjectFactory.addExpectation(m, 1)
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

    // This state occurs if, in tested mode, a method is invoked on a
    // "record type" object.  Basically, it tracks the return and
    // tells the RecordPrimary about it so that it can set up
    // constructor calls properly.  (Really, it should provide an
    // "escape" that allows it to turn the RecordPrimary into a Mocked
    // if anything too complicated happens.)
    private class RecordPrimaryInvocation extends PostCallState {
        private final PreCall openingCall;
        private final State continuation;
        private final RecordPrimary receiver;

        private RecordPrimaryInvocation(PreCall openingCall,
                                        State continuation) {
            this.openingCall = openingCall;
            this.continuation = continuation;

            ProgramObject p = getProgramObject(openingCall.receiver);

            assert p instanceof RecordPrimary;
            receiver = (RecordPrimary) p;
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

                receiver.returnsFromMethod(openingCall.method,
                                           m);
            }
            
            setState(continuation);
        }
    }

    private class Idle implements State {
        public void process(TraceEvent te) {
        }
    }

    public static Set<Instance> readPotentialRecordPrimaries(String rpDump)
        throws FileNotFoundException {
        Set<Instance> rps = new HashSet<Instance>();

        InputStream rpIn = new FileInputStream(rpDump);
        Deserializer<Instance>  rpDeserializer
            = Deserializer.getDeserializer(rpIn, Instance.class);
        
        while (true) {
            Instance rp = rpDeserializer.read();
            
            if (rp == null) {
                break;
                
            }
            
            rps.add(rp);
        }
        return rps;
    }
        

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 6) {
            throw new RuntimeException("usage: Processor trace-file tcg-dump-out test-case-name test-method-name tested-class");
        }

        String traceFileName = args[0];
        String tcgDump = args[1];
        String rpDump = args[2];
        String testCaseName = args[3];
        String testMethodName = args[4];
        String testedClass = args[5];

        TestCaseGenerator tcg = new TestCaseGenerator(testCaseName);
        TestMethodGenerator tmg = new TestMethodGenerator(testMethodName, tcg,
                                                          true);
        tcg.addChunk(tmg);

        InputStream in = new FileInputStream(traceFileName);
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);

        Set<Instance> rps = readPotentialRecordPrimaries(rpDump);

        Processor p = new Processor(d, tmg, testedClass, rps);
        p.process();

        PrintStream ps = new PrintStream(tcgDump);
        Serializer<TestCaseGenerator> s = Serializer.getSerializer(ps);

        s.write(tcg);
        s.close();
    }
}
