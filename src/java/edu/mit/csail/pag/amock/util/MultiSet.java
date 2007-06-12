package edu.mit.csail.pag.amock.util;

import java.util.*;
import java.io.Serializable;

// Adapted from PAG's "joe" project.

public class MultiSet<T> implements Serializable {
    private final Map<T, Integer> frequencyMap  = new LinkedHashMap<T, Integer>();
    
    public void add(T o) {
        add(o, 1);
    }

    public void add(T o, int multiplicity) {
        Integer i = frequencyMap.get(o);
        if (i == null) {
            i = 0;
        }
        frequencyMap.put(o, i + multiplicity);
    }

    public void addAll(Collection<T> os) {
        for (T o : os) {
            add(o);
        }
    }

    public void addAll(MultiSet<T> os) {
        for (T o : os.elementsAsSet()) {
            add(o, os.getMultiplicity(o));
        }
    }
    
    public void remove(T o) {
        Integer i = frequencyMap.get(o);
        if(i == null || i < 1) {
            throw new IllegalStateException("Value not found when trying to remove from multiset. Value: " + o);
        } 
        if (i == 1) {
            frequencyMap.remove(o);
        } else {
            frequencyMap.put(o, i - 1);
        }
    }

    public Set<T> elementsAsSet() {
        return frequencyMap.keySet();
    }

    public boolean isEmpty() {
        return frequencyMap.isEmpty();
    }

    public int getMultiplicity(T o) {
        Integer i = frequencyMap.get(o);
        if (i == null) {
            i = 0;
        }
        return i;
    }

    public static <S> MultiSet<S> singleton(S o) {
        MultiSet<S> s = new MultiSet<S>();
        s.add(o);
        return s;
    }
}
