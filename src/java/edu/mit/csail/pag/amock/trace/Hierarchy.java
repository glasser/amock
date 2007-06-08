package edu.mit.csail.pag.amock.trace;

import jpaul.Graphs.*;
import java.util.*;
import java.io.*;

public class Hierarchy {
    private final DiGraph<String> classGraph;
    private final Map<String, HierarchyEntry> entriesByName;
    
    public Hierarchy(Collection<HierarchyEntry> entries) {
        this.entriesByName = createEntryMap(entries);

        this.classGraph
            = DiGraph.diGraph(entriesByName.keySet(),
                              new ForwardNavigator<String>() {
                                  public List<String> next(String c) {
                                      if (entriesByName.containsKey(c)) {
                                          return allParents(entriesByName.get(c));
                                      } else {
                                          return Collections.emptyList();
                                      }
                                  }
                              });
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
}
