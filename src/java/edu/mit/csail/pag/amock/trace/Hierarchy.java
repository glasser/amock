package edu.mit.csail.pag.amock.trace;

import jpaul.Graphs.*;
import java.util.*;
import java.io.*;

public class Hierarchy {
    private final DiGraph<String> classGraph;
    private final Map<String, HierarchyEntry> entriesByName;
    
    public Hierarchy(Collection<HierarchyEntry> entries) {
        this.entriesByName = createEntryMap(entries);

        final ForwardNavigator<String> nav = new ForwardNavigator<String>() {
            public List<String> next(String c) {
                if (entriesByName.containsKey(c)) {
                    return allParents(entriesByName.get(c));
                } else {
                    return Collections.emptyList();
                }
            }
        };
        
        this.classGraph = new DiGraph<String>(true) { // turn on caching!
            public Collection<String> getRoots() {
                return entriesByName.keySet();
            }
            public ForwardNavigator<String> getForwardNavigator() {
                return nav;
            }
        };
    }

    public static Hierarchy createFromFile(String dumpFile)
        throws FileNotFoundException {
        InputStream in = new FileInputStream(dumpFile);
        Deserializer<HierarchyEntry> d
            = Deserializer.getDeserializer(in, HierarchyEntry.class);
        
        final List<HierarchyEntry> entries = new ArrayList<HierarchyEntry>();
        d.process(new TraceProcessor<HierarchyEntry>() {
                public void processEvent(HierarchyEntry he) {
                    entries.add(he);
                }});
        
        return new Hierarchy(entries);
    }

    private static Map<String, HierarchyEntry> createEntryMap(Collection<HierarchyEntry> entries) {
        Map<String, HierarchyEntry> entriesByName
            = new HashMap<String, HierarchyEntry>();

        for (HierarchyEntry he : entries) {
            entriesByName.put(he.className, he);
        }

        return entriesByName;
    }

    private static List<String> allParents(HierarchyEntry he) {
        List<String> ret = new ArrayList<String>();
        ret.add(he.superName);
        
        for (String iface : he.interfaces) {
            ret.add(iface);
        }

        return ret;
    }

    // transitive and reflexive
    public Collection<String> allKnownAncestors(String cls) {
        return classGraph.transitiveSucc(cls);
    }

    public boolean isKnownPublicClass(String cls) {
        HierarchyEntry he = entriesByName.get(cls);
        return he != null && he.isPublic;
    }

    /**
     * Returns a class name that baseClass extends or implements (or
     * is) which extends or implements all of the classes in
     * mustImplement, and is most general among such classes.
     */
    public String getMostGeneralClass(final String baseClass,
                                      final Collection<String> mustImplement) {
        if (! classGraph.vertices().contains(baseClass)) {
            // Don't have any information about it... keep it as
            // itself.
            return baseClass;
        }

        Set<String> ancestors = classGraph.transitiveSucc(baseClass);

        for (String supe : mustImplement) {
            // Don't know about this thing that we need to implement,
            // or at least don't know that baseClass implements it.
            // Bail.
            if (! ancestors.contains(supe)) {
                return baseClass;
            }
        }

        // Easy answer: if we need to be ourselves, just be it.
        if (mustImplement.contains(baseClass)) {
            return baseClass;
        }

        if (mustImplement.isEmpty()) {
            return "java/lang/Object";
        }

        Set<String> implementorsOfAll = null;
        for (String supe : mustImplement) {
            Set<String> implementorsOfThisOne
                = classGraph.transitivePred(supe);
            
            if (implementorsOfAll == null) {
                implementorsOfAll = implementorsOfThisOne;
            } else {
                implementorsOfAll.retainAll(implementorsOfThisOne);
            }
        }

        DiGraph<String> ancestorGraph = classGraph.subDiGraph(implementorsOfAll);

        ForwardNavigator<String> nav = ancestorGraph.getForwardNavigator();

        assert ancestorGraph.vertices().contains(baseClass);
        
        for (String remaining : ancestorGraph.vertices()) {
            if (nav.next(remaining).isEmpty()) {
                // We found a leaf!
                return remaining;
            }
        }

        throw new IllegalStateException("Ancestry not a DAG!");
    }
}
