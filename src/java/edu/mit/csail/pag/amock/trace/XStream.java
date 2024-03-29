package edu.mit.csail.pag.amock.trace;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.hooks.*;

public class XStream extends com.thoughtworks.xstream.XStream {
    {
        alias("pre-call", PreCall.class);
        alias("post-call", PostCall.class);
        alias("method-entry", MethodEntry.class);
        alias("method-exit", MethodExit.class);
        alias("clinit-entry", ClinitEntry.class);
        alias("clinit-exit", ClinitExit.class);
        alias("field-read", FieldRead.class);
        alias("instance", Instance.class);
        alias("primitive", Primitive.class);
        alias("void-return-value", VoidReturnValue.class);
        alias("initializing-object", ConstructorReceiver.class);
        alias("field", TraceField.class);
        alias("method", TraceMethod.class);
        alias("record-info", RecordPrimaryClassInfo.class);
        alias("iteration-info", IterationPrimaryClassInfo.class);
        alias("static-fields", SFPCIList.class);
        alias("all-fields", SFPCIAll.class);
        alias("class", HierarchyEntry.class);
        
        useAttributeFor("id", int.class);
        useAttributeFor("className", String.class);
        useAttributeFor("callId", int.class);
        useAttributeFor("name", String.class);
        useAttributeFor("nameWithSlashes", String.class);
        aliasAttribute("n", "nameWithSlashes");
    }
}
