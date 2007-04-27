package edu.mit.csail.pag.amock.trace;

import edu.mit.csail.pag.amock.representation.ProgramObject;

import java.io.Serializable;

public class Primitive extends TraceObject implements ProgramObject,
                                                      Serializable {
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

    public void incrementReferenceCount() {
        // Do nothing.
    }
}
