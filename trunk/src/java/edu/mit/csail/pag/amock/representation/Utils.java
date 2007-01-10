package edu.mit.csail.pag.amock.representation;

import java.util.regex.*;

public class Utils {
    private static final Pattern LAST_PART
        = Pattern.compile("\\.(\\w+)$");

    public static String classNameWithoutPackage(String longName) {
        Matcher m = LAST_PART.matcher(longName);

        if (! m.find()) {
            throw new RuntimeException("Weird class name: " + longName);
        }

        return m.group(1);

    }
}