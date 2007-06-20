package edu.mit.csail.pag.amock.processor;

import java.io.*;
import java.util.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;

public class Processor implements TraceProcessor<TraceEvent> {
    private final ClassName testedClass;

    private final BoundaryTranslator boundary;
    
    private final ProgramObjectFactory programObjectFactory;

    private State state;
    {
        setState(new WaitForCreation());
    }

    public Processor(ProgramObjectFactory programObjectFactory,
                     ClassName testedClass,
                     Map<Instance, InstanceInfo> instanceInfos,
                     Hierarchy hierarchy) {
        this.programObjectFactory = programObjectFactory;
        this.testedClass = testedClass;

        this.boundary = new HeuristicBoundaryTranslator(programObjectFactory,
                                                        instanceInfos,
                                                        hierarchy);
    }

    public void processEvent(TraceEvent ev) {
        this.state.processEvent(ev);
    }

    private void setState(State newState) {
        state = newState;
        // One can, say, print out newState.getClass().getSimpleName() here
    }

    private ProgramObject getProgramObject(TraceObject t) {
        return boundary.traceToProgram(t, false);
    }

    private ProgramObject getProgramObjectForReturnAction(TraceObject t) {
        return boundary.traceToProgram(t, true);
    }

    private ProgramObject[] getProgramObjects(TraceObject[] tos) {
        ProgramObject[] pos = new ProgramObject[tos.length];
        for (int i = 0; i < tos.length; i++) {
            pos[i] = getProgramObject(tos[i]);
        }
        return pos;
    }

    private interface State extends TraceProcessor<TraceEvent> {}

    private abstract class PreCallState implements State {
        public void processEvent(TraceEvent ev) {
            if (!(ev instanceof PreCall)) {
                return;
            }

            processPreCall((PreCall) ev);
        }
        abstract public void processPreCall(PreCall p);
    }

    private abstract class PostCallState implements State {
        public void processEvent(TraceEvent ev) {
            if (!(ev instanceof PostCall)) {
                return;
            }

            processPostCall((PostCall) ev);
        }
        abstract public void processPostCall(PostCall p);
    }

    private abstract class CallState implements State {
        public void processEvent(TraceEvent ev) {
            if (ev instanceof PreCall) {
                processPreCall((PreCall) ev);
            } else if (ev instanceof PostCall) {
                processPostCall((PostCall) ev);
            } else if (ev instanceof FieldRead) {
                processFieldRead((FieldRead) ev);
            } else if (ev instanceof MethodEntry) {
                processMethodEntry((MethodEntry) ev);
            }
        }
        abstract public void processPreCall(PreCall p);
        abstract public void processPostCall(PostCall p);
        public void processFieldRead(FieldRead fr) {
            // Do nothing, by default.
        }
        public void processMethodEntry(MethodEntry m) {
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

            programObjectFactory.prepareForNewPrimaryExecution();
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

            if (p.isStatic()) {
                if (! testedClass.equals(p.method.declaringClass)) {
                    // XXX: should be more nuanced
                    setState(new MockModeNested(p, this));
                }
                return;
            }
            
            if (boundary.isKnownPrimary(p.receiver)) {
                ProgramObject rec = getProgramObject(p.receiver);
                if (rec instanceof RecordPrimary) {
                    setState(new RecordPrimaryInvocation(p, this));
                    return;
                } else if (rec instanceof IterationPrimary) {
                    setState(new IterationPrimaryInvocation(p, this));
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
            ClassName instanceClassName
                = ((Instance) openingCall.receiver).className;

            Primary primary
                = programObjectFactory.addPrimary(instanceClassName,
                                                  openingCall.method,
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
        private final StaticTarget staticTarget;

        private MockModeNested(PreCall openingCall, State continuation) {
            this.openingCall = openingCall;
            this.continuation = continuation;

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

            this.expectation =
                programObjectFactory.addExpectation(target, 1)
                .method(openingCall.method);

            if (openingCall.args.length == 0) {
                expectation.withNoArguments();
            } else {
                expectation.withArguments(getProgramObjects(openingCall.args));
            }
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

    // This state occurs if, in tested mode, a method is invoked on an
    // "iteration pattern" object.  Basically, if it's "next", then we
    // see what it returns and add that to the construction of the
    // iterator; otherwise (it's something like hasNext) we ignore it.
    // (We know it can only be these things since otherwise it
    // wouldn't have been selected as IterationPrimary.)
    private class IterationPrimaryInvocation extends PostCallState {
        private final PreCall openingCall;
        private final State continuation;
        private final IterationPrimary receiver;

        private IterationPrimaryInvocation(PreCall openingCall,
                                           State continuation) {
            this.openingCall = openingCall;
            this.continuation = continuation;

            ProgramObject p = getProgramObject(openingCall.receiver);

            assert p instanceof IterationPrimary;
            receiver = (IterationPrimary) p;
        }

        public void processPostCall(PostCall p) {
            if (p.callId != openingCall.callId) {
                // TODO: maybe this was a callback?
                return;
            }

            TraceObject ret = p.returnValue;

            if (!(ret instanceof VoidReturnValue)) {
                ProgramObject retPO = getProgramObject(ret);

                receiver.returnsFromMethod(openingCall.method,
                                           retPO);
            }
            
            setState(continuation);
        }
    }

    private class Idle implements State {
        public void processEvent(TraceEvent te) {
        }
    }

    public static Map<Instance, InstanceInfo> readInstanceInfos(String iiDump)
        throws FileNotFoundException {
        final Map<Instance, InstanceInfo> iis
            = new HashMap<Instance, InstanceInfo>();

        InputStream iiIn = new FileInputStream(iiDump);
        Deserializer<InstanceInfo>  iiDeserializer
            = Deserializer.getDeserializer(iiIn, InstanceInfo.class);

        iiDeserializer.process(new TraceProcessor<InstanceInfo>() {
                public void processEvent(InstanceInfo ii) {
                    iis.put(ii.instance, ii);
                }
            });

        return iis;
    }
        

    public static void main(String args[]) throws FileNotFoundException {
        // TODO: use sane arg parsing
        if (args.length != 7) {
            throw new RuntimeException("usage: Processor trace-file tcg-dump-out inst-info-dump hierarchy-dump test-case-name test-method-name tested-class");
        }

        String traceFileName = args[0];
        String tcgDump = args[1];
        String iiDump = args[2];
        String hierDump = args[3];
        String testCaseName = args[4];
        String testMethodName = args[5];
        ClassName testedClass = ClassName.fromSlashed(args[6]);

        Hierarchy hierarchy = Hierarchy.createFromFile(hierDump);

        TestCaseGenerator tcg = new TestCaseGenerator(testCaseName);
        TestMethodGenerator tmg = new TestMethodGenerator(testMethodName,
                                                          hierarchy,
                                                          true);
        tcg.addChunk(tmg);

        InputStream in = new FileInputStream(traceFileName);
        Deserializer<TraceEvent> d
            = Deserializer.getDeserializer(in, TraceEvent.class);

        Map<Instance, InstanceInfo> iis = readInstanceInfos(iiDump);


        Processor p = new Processor(tmg, testedClass, iis, hierarchy);
        d.process(p);

        PrintStream ps = new PrintStream(tcgDump);
        Serializer<TestCaseGenerator> s = Serializer.getSerializer(ps);

        s.write(tcg);
        s.close();
    }
}
