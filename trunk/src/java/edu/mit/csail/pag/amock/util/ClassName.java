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

    private ClassName(String nameWithSlashes) {
        this.nameWithSlashes = nameWithSlashes;
    }
    
    public static ClassName fromDotted(String nameDotted) {
        assert ! nameDotted.contains("/");
        return new ClassName(nameDotted.replace(".", "/"));
    }

    public static ClassName fromSlashed(String nameSlashed) {
        assert ! nameSlashed.contains(".");
        return new ClassName(nameSlashed);
    }

    public String dotted() {
        return this.nameWithSlashes.replace("/", ".");

    }

    public String slashed() {
        return this.nameWithSlashes;
    }

    public Type getObjectType() {
        return Type.getType("L" + this.slashed() + ";");
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

    @Override public boolean equals(Object o) {
        if (!(o instanceof ClassName)) {
            return false;
        }
        ClassName other = (ClassName) o;

        return this.nameWithSlashes.equals(other.nameWithSlashes);
    }

    @Override public int hashCode() {
        return this.nameWithSlashes.hashCode()*3;
    }

    public int compareTo(ClassName o) {
        return this.nameWithSlashes.compareTo(o.nameWithSlashes);
    }

    @Override public String toString() {
        // While converting everything to use ClassName, I do *not*
        // want code to accidentally use toString() instead of
        // choosing which kind it wants!
        throw new RuntimeException("don't call toString!");
    }

    public boolean isInDefaultPackage() {
        return ! this.nameWithSlashes.contains("/");
    }

    public boolean isNestedClass() {
        return this.nameWithSlashes.contains("$");
    }
}
