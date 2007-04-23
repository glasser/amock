package edu.mit.csail.pag.amock.trace;

public class XStream extends com.thoughtworks.xstream.XStream {
    {
        alias("pre-call", PreCall.class);
        alias("post-call", PostCall.class);
        alias("method-entry", MethodEntry.class);
        alias("method-exit", MethodExit.class);
        alias("field-read", FieldRead.class);
        alias("instance", Instance.class);
        alias("primitive", Primitive.class);
        alias("void-return-value", VoidReturnValue.class);
        alias("initializing-object", ConstructorReceiver.class);
        
        useAttributeFor("id", int.class);
        useAttributeFor("className", String.class);
        useAttributeFor("callId", int.class);
    }
}
