package edu.mit.csail.pag.amock.subjects.fields;

import java.awt.Rectangle;

public class RectangleHelper {
    private final Rectangle r;
    public RectangleHelper(Rectangle r) {
        this.r = r;
    }

    public int returnXMaybeTweak(boolean tweak) {
        int x = r.x;
        int y = r.y;

        if (tweak) {
            r.setLocation(x+5, y);
        }

        return x;
    }
}

