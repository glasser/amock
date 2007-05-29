package edu.mit.csail.pag.amock.util;

import java.util.regex.*;

import org.objectweb.asm.Type;

public class Misc {
    private static final Pattern LAST_PART
        = Pattern.compile("\\.(\\w+)$");

    public static String classNameWithoutPackage(String longName) {
        Matcher m = LAST_PART.matcher(longName);

        if (! m.find()) {
            // There's no package.
            return longName;
        }

        return m.group(1);

    }

    // This function is in ASM 3.0 but not 2.x.
    public static Type getObjectType(String className) {
        return Type.getType("L"+className+";");
    }

    public static String classNameSlashesToPeriods(String in) {
        return getObjectType(in).getClassName();
    }

    public static String classNamePeriodsToSlashes(String in) {
        return in.replace('.', '/');
    }
}