package edu.mit.csail.pag.amock.util;

import org.objectweb.asm.Type;
import java.util.regex.*;
import java.io.Serializable;

/**
 * Some Java program analysis tools represent class names with periods
 * as package separators, and others use slashes (following the Java
 * language or the JVM respectively).  This class abstracts dealing
 * with this away.
 */
public final class ClassName implements Comparable<ClassName>, Serializable {
    private final String nameWithSlashes;
    private final int arrayLevels;

    private ClassName(String name) {
        int a = 0;
        while (name.endsWith("[]")) {
            a++;
            name = name.substring(0, name.length()-2);
        }

        this.arrayLevels = a;
        this.nameWithSlashes = name;
    }
    
    public static ClassName fromDotted(String nameDotted) {
        assert ! nameDotted.contains("/");
        return new ClassName(nameDotted.replace(".", "/"));
    }

    public static ClassName fromSlashed(String nameSlashed) {
        assert ! nameSlashed.contains(".");
        return new ClassName(nameSlashed);
    }

    public static ClassName fromClass(Class<?> cls) {
        String name = cls.getName();

        int arrayLevels = 0;

        while (name.startsWith("[")) {
            arrayLevels++;
            name = name.substring(1, name.length());
        }

        if (arrayLevels > 0) {
            if (name.startsWith("L") && name.endsWith(";")) {
                name = name.substring(1, name.length()-1);
            } else {
                // It's a primitive.  don't translate, because I'm
                // lazy XXX TODO
            }

            // XXX should use buffer
            while (arrayLevels > 0) {
                name = name + "[]";
                arrayLevels--;
            }
        }

        return fromDotted(name);
    }

    public static ClassName fromType(Type t) {
        return fromDotted(t.getClassName());
    }

    public String dotted() {
        return slashed().replace("/", ".");
    }

    public String slashed() {
        StringBuilder sb = new StringBuilder(this.nameWithSlashes);
        for (int i = 0; i < arrayLevels; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    public Type getObjectType() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayLevels; i++) {
            sb.append("[");
        }
        sb.append("L");
        sb.append(this.nameWithSlashes);
        sb.append(";");
        return Type.getType(sb.toString());
    }

    public String asClassForNameArgument() {
        if (this.arrayLevels == 0) {
            return dotted();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayLevels; i++) {
            sb.append("[");
        }
        sb.append("L");
        sb.append(this.nameWithSlashes.replace("/", "."));
        sb.append(";");
        return sb.toString();
    }

    private static final Pattern LAST_PART
        = Pattern.compile("(/|\\$)(\\w+)$");
    public String classNameWithoutPackage() {
        Matcher m = LAST_PART.matcher(this.nameWithSlashes);

        if (! m.find()) {
            // There's no package.
            return this.nameWithSlashes;
        }

        return m.group(2);
    }

    // This is a bug in the tracer.
    public boolean isBrokenXXX() {
        return nameWithSlashes.startsWith("[");
    }

    public String dottedPackageName() {
        int lastBitLength = classNameWithoutPackage().length();
        int slashedLength = this.nameWithSlashes.length();
        return this.nameWithSlashes.substring(0, slashedLength - lastBitLength - 1)
            .replace("/", ".");
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof ClassName)) {
            return false;
        }
        ClassName other = (ClassName) o;

        return this.nameWithSlashes.equals(other.nameWithSlashes)
            && this.arrayLevels == other.arrayLevels;
    }

    @Override public int hashCode() {
        return this.nameWithSlashes.hashCode()*3 + this.arrayLevels;
    }

    public int compareTo(ClassName o) {
        return this.nameWithSlashes.compareTo(o.nameWithSlashes);
    }

    @Override public String toString() {
        // Note that I want every use to be .dotted() or .slashed(),
        // so this is explicitly *not* one of those...
        return "CLS[" + dotted() + "]";
    }

    public boolean isInDefaultPackage() {
        return ! this.nameWithSlashes.contains("/");
    }

    public boolean isNestedClass() {
        return this.nameWithSlashes.contains("$");
    }

    public boolean isJavaLangObject() {
        return this.nameWithSlashes.equals("java/lang/Object")
            && this.arrayLevels == 0;
    }

    public boolean isArray() {
        return this.arrayLevels > 0;
    }
}
