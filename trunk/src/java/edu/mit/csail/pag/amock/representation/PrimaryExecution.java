package edu.mit.csail.pag.amock.representation;

import java.util.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

import org.objectweb.asm.Type;

public class PrimaryExecution implements CodeChunk {
    private final Primary primary;
    private final TraceMethod method;
    private final ProgramObject[] arguments;

    private ProgramObject equalToThis = null;
    private String methodNameForIs;
    private String methodNameForIsNull;
    private String methodNameForAssertThat;
    private String castClassName;

    public PrimaryExecution(Primary primary,
                            TraceMethod method,
                            ProgramObject[] arguments) {
        this.primary = primary;
        this.method = method;
        this.arguments = arguments;
    }

    private boolean needsAssertion() {
        return this.equalToThis != null;
    }

    private boolean assertingNull() {
        return this.equalToThis instanceof Primitive
            && ((Primitive) this.equalToThis).value == null;
    }
    
    // TODO should only do this if differs from po's type
    private boolean assertionWouldNeedCast() {
        return getReturnValueType().getSort() == Type.OBJECT;
    }

    private Type getReturnValueType() {
        return Type.getReturnType(this.method.descriptor);
    }
    
    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        primary.resolveNames(cr, vr);
        for (ProgramObject po : arguments) {
            po.resolveNames(cr, vr);
        }
        
        if (needsAssertion()) {
            this.equalToThis.resolveNames(cr, vr);

            this.methodNameForIs =
                cr.getStaticMethodName(ClassName.fromDotted("org.hamcrest.core.Is"),
                                       "is");
            this.methodNameForAssertThat =
                cr.getStaticMethodName(ClassName.fromDotted("org.hamcrest.MatcherAssert"),
                                       "assertThat");
            if (assertingNull()) {
                this.methodNameForIsNull =
                    cr.getStaticMethodName(ClassName.fromDotted("org.hamcrest.core.IsNull"),
                                           "nullValue");
            } else {
                if (assertionWouldNeedCast()) {
                    this.castClassName =
                        cr.getSourceName(ClassName.fromDotted(getReturnValueType().getClassName()));
                }
            }
        }
    }

    public PrimaryExecution isEqualTo(ProgramObject po) {
        assert this.equalToThis == null;
        this.equalToThis = po;

        return this;
    }

    private void printAssertion(LinePrinter p) {
        assert needsAssertion();
        
        String whatItIs = "";

        if (assertingNull()) {
            whatItIs = this.methodNameForIsNull + "()";
        } else {
            if (assertionWouldNeedCast()) {
                whatItIs = "(" + this.castClassName + ") ";
            }

            
            whatItIs += this.equalToThis.getSourceRepresentation();
        }

        p.line(this.methodNameForIs + "(" + whatItIs + ")");
    }

    public void printSource(LinePrinter p) {
        StringBuilder s = new StringBuilder();

        if (needsAssertion()) {
            s.append(this.methodNameForAssertThat);
            s.append("(");
        }
        
        s.append(primary.getSourceRepresentation());
        s.append(".");
        s.append(method.name);
        s.append("(");

        boolean first = true;
        for (ProgramObject argument : arguments) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            s.append(argument.getSourceRepresentation());
        }
        s.append(")");

        if (needsAssertion()) {
            s.append(",");
        
            p.line(s.toString());
            printAssertion(new IndentingLinePrinter(p, 2));
            p.line(");");
        } else {
            s.append(";");
            p.line(s.toString());
        }
    }

    public MultiSet<ProgramObject> getProgramObjects() {
        MultiSet<ProgramObject> pos = new MultiSet<ProgramObject>();
        pos.add(primary);
        pos.addAll(Arrays.asList(arguments));
        if (this.equalToThis != null) {
            pos.add(this.equalToThis);
        }
        return pos;
    }
}
