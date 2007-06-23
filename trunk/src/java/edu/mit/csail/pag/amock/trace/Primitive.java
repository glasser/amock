package edu.mit.csail.pag.amock.trace;

import org.objectweb.asm.Type;

import edu.mit.csail.pag.amock.representation.*;

import java.io.Serializable;

public class Primitive extends AbstractProgramObject
    implements TraceObject, Serializable {
    public final Object value;

    public Primitive(Object value) {
        this.value = value;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Primitive)) {
            return false;
        }
        Primitive p = (Primitive) o;

        if (value == null || p.value == null) {
            return value == null && p.value == null;
        }

        return value.equals(p.value);
    }

    @Override public int hashCode() {
        if (value == null) {
            return 12345; // arbitrary
        } else {
            return value.hashCode() * 3;
        }
    }

    // Implements ProgramObject method.
    public String getSourceRepresentation() {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return quoteString((String) value);
        } else if (value instanceof Character) {
            return "'" + quoteChar((Character) value) + "'";
        } else if (value instanceof Long) {
            long v = (Long) value;
            if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
                return value.toString() + "L";
            } else {
                return value.toString();
            }
        } else {
            // Should be numeric.
            return value.toString();
        }
    }
        
    private String quoteString(String unformatted) {
        StringBuffer formatted = new StringBuffer();
        formatted.append('"');
        for (int i = 0; i < unformatted.length(); i++) {
            formatted.append(quoteChar(unformatted.charAt(i)));
        }
        formatted.append('"');
        
        return formatted.toString();
    }

    private String quoteChar(char ch) {
        switch (ch) {
        case '"':
            return "\\\"";
        
        case '\n':
            return "\\n";
            
        case '\r':
            return "\\r";
        
        case '\t':
            return "\\t";
        
        default:
            return new String(new char[]{ch});
        }
    }

    public void usedAsType(Type t) {
        // XXX: Should probably just assert that numeric, string,
        // object, and array don't mix or something.
    }

    public void resolveNames(ClassNameResolver cr,
                             VariableNameBaseResolver vr) {
        // Do nothing.
    }
}
