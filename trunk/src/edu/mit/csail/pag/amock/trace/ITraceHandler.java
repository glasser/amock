package edu.mit.csail.pag.amock.trace;

import java.io.PrintStream;

public interface ITraceHandler {

    public void arrayload(Object val, int index, Object arr);

    public void getfield(Object val, Object obj, String field_name);

    public void arraystore(Object arr, int index, Object val);

    public void putfield(Object obj, Object val, String field_name);
    
    public void putstatic(Object val, String field_name);

    public int enter(int call_id, Object receiver, Object[] args, String method_signature);

    public void trace(Object ret_val, Object receiver, Object[] args, String signature, int enter_indent, int call_id);

    public void stop();

    public void reset();

    //XXX nasty to have it here
    public void setTraceFile(PrintStream stream);

    //XXX nasty to have it here
    public void setParameterize(boolean b);

    //XXX nasty to have it here
    public void setOutputFileName(String name);
}
