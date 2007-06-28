package edu.mit.csail.pag.amock.processor;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.util.*;
    
// TESTED MODE: parent of TestedModeMain and UnmockableStaticMethod
public abstract class TestedState extends CallState {
    protected final PreCall openingCall;
    protected final State continuation;

    protected TestedState(PreCall openingCall,
                          State continuation,
                          Processor p) {
        super(p);
        this.openingCall = openingCall;
        this.continuation = continuation;
    }

    public void processPreCall(PreCall p) {
        if (p.isConstructor() && p.isTopLevelConstructor) {
            // We'll go into a nested version of TestedModeMain to
            // deal with this.  The object being constructed
            // should be an InternalPrimary.
            setState(new TestedModeMain(p, null, this, false, getProcessor()));
            return;
        }

        if (p.isStatic()) {
            if (! getTestedClass().equals(p.method.declaringClass)) {
                if (Premain.shouldTransform(p.method.declaringClass)) {
                    // XXX: should be more nuanced
                    setState(new MockModeNested(p, this, getProcessor()));
                } else {
                    // They called a static method in the JDK or
                    // something like that.  We want to treat this
                    // roughly like a constructor call: make an
                    // internal primary.
                    setState(new UnmockableStaticMethod(p, this, getProcessor()));
                }
            }
            return;
        }
            
        if (boundary().isKnownPrimary(p.receiver)) {
            ProgramObject rec = getProgramObject(p.receiver);
            if (rec instanceof RecordPrimary) {
                setState(new RecordPrimaryInvocation(p, this, getProcessor()));
                return;
            } else if (rec instanceof IterationPrimary) {
                setState(new IterationPrimaryInvocation(p, this, getProcessor()));
                return;
            }
        }

        maybeMock(p);
    }

    // Note: can't pass in something that's static, or a top-level
    // constructor, etc...
    private void maybeMock(MethodStartEvent p) {
        if (boundary().isKnownMocked(p.receiver) &&
            ! p.isConstructor()) {
            
            setState(new MockModeNested(p, this, getProcessor()));
            return;
        }
        
        // Ignore, because it's part of the tested code
        // itself, or maybe a non-top-level constructor.
    }

    @Override public void processMethodEntry(MethodEntry m) {
        if (m.fromUninstrumentedCode) {
            maybeMock(m);
        }
    }

    public void processFieldRead(FieldRead fr) {
        // If we observe a read from a mock, it better have the
        // right value!
        if (boundary().isKnownMocked(fr.receiver)) {
            Mocked receiver = (Mocked) getProgramObject(fr.receiver);
            ProgramObject value = getProgramObject(fr.value);
            programObjectFactory().tweakState(receiver,
                                              fr.field,
                                              value);
        } else if (boundary().isKnownPrimary(fr.receiver)) {
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
