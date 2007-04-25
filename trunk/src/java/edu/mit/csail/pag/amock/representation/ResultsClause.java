package edu.mit.csail.pag.amock.representation;

public class ResultsClause implements CodeChunk {
    private String returnValue;
    
    public void willReturnValue(String returnValue) {
        assert this.returnValue == null;
        this.returnValue = returnValue;
    }

    public void tweakStatement(String statement) {
        // XXX
    }

    public void printSource(LinePrinter p) {
        if (returnValue != null) {
            p.line("will(returnValue(" + returnValue + "));");
        }
        // XXX tweaks
    }
}
