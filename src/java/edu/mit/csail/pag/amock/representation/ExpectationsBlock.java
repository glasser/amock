package edu.mit.csail.pag.amock.representation;

/**
 * A series of chunks inside a jMock 2 expects() call.
 */

import edu.mit.csail.pag.amock.util.ClassName;

public class ExpectationsBlock extends IndentingEmptyLineSeparatedCodeBlock {
    private static final ClassName GROUP_BUILDER_CLASS
        = ClassName.fromDotted("edu.mit.csail.pag.amock.jmock.Expectations");
    
    private String groupBuilderClassShortName = null;
    private boolean empty = true;

    public void printSource(LinePrinter lp) {
        if (!empty) {
            lp.line("verifyThenCheck(new "
                    + groupBuilderClassShortName + "() {{");
            super.printSource(lp);
            lp.line("}});");
        }
    }

    @Override public void addChunk(CodeChunk c) {
        empty = false;
        super.addChunk(c);
    }

    @Override public void resolveNames(ClassNameResolver cr,
                                       VariableNameBaseResolver vr) {
        super.resolveNames(cr, vr);
        if (!empty && groupBuilderClassShortName == null) {
            groupBuilderClassShortName
                = cr.getSourceName(GROUP_BUILDER_CLASS);
        }
    }
}
