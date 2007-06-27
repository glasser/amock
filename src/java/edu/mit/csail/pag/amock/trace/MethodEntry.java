package edu.mit.csail.pag.amock.trace;

public class MethodEntry extends MethodStartEvent {
    private final boolean fromUninstrumentedCode;
    public MethodEntry(int callId,
                       TraceMethod method,
                       TraceObject receiver,
                       TraceObject[] args,
                       boolean fromUninstrumentedCode) {
        super(callId, method, receiver, args);
        this.fromUninstrumentedCode = fromUninstrumentedCode;
    }

    public MethodEntry(int callId,
                       TraceMethod method,
                       TraceObject receiver,
                       TraceObject[] args) {
        this(callId, method, receiver, args, false);
    }

    public MethodEntry copyFromUninstrumented() {
        return new MethodEntry(this.callId,
                               this.method,
                               this.receiver,
                               this.args,
                               true);
    }
}
