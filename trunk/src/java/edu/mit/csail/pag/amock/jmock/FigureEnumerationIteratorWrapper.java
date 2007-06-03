package edu.mit.csail.pag.amock.jmock;

import java.util.Iterator;
import java.util.Arrays;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

public class FigureEnumerationIteratorWrapper implements FigureEnumeration {
    private final Iterator<Figure> it;
    
    public FigureEnumerationIteratorWrapper(Figure... items) {
        this.it = Arrays.asList(items).iterator();
    }
    
    public boolean hasMoreElements() {
        return it.hasNext();
    }
    
    public Object nextElement() {
        return it.next();
    }
    
    public Figure nextFigure() {
        return it.next();
    }
}
