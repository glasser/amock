package edu.mit.csail.pag.amock.trace;

import jpaul.Graphs.*;
import java.util.*;
import java.io.*;

import edu.mit.csail.pag.amock.util.ClassName;

public class Hierarchy implements Serializable {
    private DiGraph<ClassName> classGraph;
    private final Map<ClassName, HierarchyEntry> entriesByName;

    private interface FN extends ForwardNavigator<ClassName>, Serializable {}
    private abstract class DG extends DiGraph<ClassName> implements Serializable {
        public DG() {
            super(true); // turn on caching!
        }
    }
    
    public Hierarchy(Collection<HierarchyEntry> entries) {
        this.entriesByName = new HashMap<ClassName, HierarchyEntry>();
        addEntriesToGraph(entries);
    }

    private void addEntriesToGraph(Collection<HierarchyEntry> entries) {
        addEntriesToMap(entries);

        final ForwardNavigator<ClassName> nav = new FN()  {
            public List<ClassName> next(ClassName c) {
                if (entriesByName.containsKey(c) && !c.isJavaLangObject()) {
                    return allParents(entriesByName.get(c));
                } else {
                    return Collections.emptyList();
                }
            }
        };
        
        this.classGraph = new DG() {
            public Collection<ClassName> getRoots() {
                return entriesByName.keySet();
            }
            public ForwardNavigator<ClassName> getForwardNavigator() {
                return nav;
            }
        };
    }

    // use addEntriesToGraph instead of calling this multiple times
    private void addEntryToGraph(HierarchyEntry entry) {
        addEntriesToGraph(Collections.singletonList(entry));
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

    private void addEntriesToMap(Collection<HierarchyEntry> entries) {
        // Note that entriesByName may or may not be empty!

        // These are the ones we know we need to deal with but haven't
        // yet.
        Set<ClassName> pendingNames = new HashSet<ClassName>();
        Queue<HierarchyEntry> pending = new LinkedList<HierarchyEntry>();
        for (HierarchyEntry he : entries) {
            if (!entriesByName.containsKey(he.className) &&
                !pendingNames.contains(he.className)) {
                pending.offer(he);
                pendingNames.add(he.className);
            }
        }

        while (! pending.isEmpty()) {
            assert pending.size() == pendingNames.size();

            HierarchyEntry next = pending.remove();
            pendingNames.remove(next.className);
            entriesByName.put(next.className, next);

            for (ClassName someSuper : next.allSupers()) {
                if (entriesByName.containsKey(someSuper)
                    || pendingNames.contains(someSuper)) {
                    continue;
                }

                if (Premain.shouldTransform(someSuper)) {
                    continue;
                }

                // Aha!  It's probably some sort of JDK class or
                // something!  Get its HierarchyEntry using
                // reflection.
                HierarchyEntry superEntry
                    = HierarchyEntry.createWithReflection(someSuper);
                pending.offer(superEntry);
                pendingNames.add(superEntry.className);
            }
        }
    }

    private static List<ClassName> allParents(HierarchyEntry he) {
        List<ClassName> ret = new ArrayList<ClassName>();
        ret.add(he.superName);
        
        for (ClassName iface : he.interfaces) {
            ret.add(iface);
        }

        return ret;
    }

    // transitive and reflexive
    public Collection<ClassName> allKnownAncestors(ClassName cls) {
        return classGraph.transitiveSucc(cls);
    }

    public boolean isKnownPublicClass(ClassName cls) {
        HierarchyEntry he = entriesByName.get(cls);
        return he != null && he.isPublic;
    }

    /**
     * Returns a class name that baseClass extends or implements (or
     * is) which extends or implements all of the classes in
     * mustImplement, and is most general among such classes.
     */
    public ClassName getMostGeneralClass(final ClassName baseClass,
                                         final Collection<ClassName> mustImplement) {
        if (! classGraph.vertices().contains(baseClass)) {
            // Don't have any information about it...

            if (! Premain.shouldTransform(baseClass)) {
                // Aha, maybe reflection will help us.
                addEntryToGraph(HierarchyEntry.createWithReflection(baseClass));
            }
        }

        // Same check again, in case we managed to fix the problem
        // with reflection.
        if (! classGraph.vertices().contains(baseClass)) {
            // Don't have any information about it...  keep it as
            // itself.
            return baseClass;
        }

        Set<ClassName> ancestors = classGraph.transitiveSucc(baseClass);

        for (ClassName supe : mustImplement) {
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
            return ClassName.fromSlashed("java/lang/Object");
        }

        Set<ClassName> implementorsOfAll = null;
        for (ClassName supe : mustImplement) {
            Set<ClassName> implementorsOfThisOne
                = classGraph.transitivePred(supe);
            
            if (implementorsOfAll == null) {
                implementorsOfAll = implementorsOfThisOne;
            } else {
                implementorsOfAll.retainAll(implementorsOfThisOne);
            }
        }

        DiGraph<ClassName> ancestorGraph = classGraph.subDiGraph(implementorsOfAll);

        ForwardNavigator<ClassName> nav = ancestorGraph.getForwardNavigator();

        assert ancestorGraph.vertices().contains(baseClass);
        
        for (ClassName remaining : ancestorGraph.vertices()) {
            if (nav.next(remaining).isEmpty()) {
                // We found a leaf!
                return remaining;
            }
        }

        throw new IllegalStateException(String.format("Ancestry of %s not a DAG! %s\n",
                                                      baseClass,
                                                      ancestorGraph));
    }
}
