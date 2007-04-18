package edu.mit.csail.pag.amock.trace;

public class PostCall extends MethodEvent {
    public final TraceObject returnValue;

    public PostCall(int callId,
                    TraceMethod method,
                    TraceObject receiver,
                    TraceObject[] args,
                    TraceObject returnValue) {
        super(callId, method, receiver, args);
        this.returnValue = returnValue;
    }

    public boolean isConstructor() {
        return method.name.equals("<init>");
    }

}
