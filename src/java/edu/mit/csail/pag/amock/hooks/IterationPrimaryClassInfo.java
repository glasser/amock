package edu.mit.csail.pag.amock.hooks;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

import java.util.*;
import java.io.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class IterationPrimaryClassInfo {
    // with slashes.
    public final String className;

    public final Collection<TraceMethod> nextItemMethods
        = new HashSet<TraceMethod>();

    public final Set<TraceMethod> benignMethods
        = new HashSet<TraceMethod>();

    private static final String IPCI_DATA_DUMP_FILE
        = "src/java/edu/mit/csail/pag/amock/hooks/iteration-primary-data.xml";

    public IterationPrimaryClassInfo(String className) {
        this.className = className;
    }

    public boolean methodIsBenign(TraceMethod m) {
        return m.isConstructor()
            || benignMethods.contains(m)
            || nextItemMethods.contains(m);
    }

    public boolean methodGetsNextItem(TraceMethod m) {
        return nextItemMethods.contains(m);
    }
    
    // Note that the keys of this map have periods, not slashes.
    private static Map<String, IterationPrimaryClassInfo> cachedClassInfo;

    private static void initializeCache() {
        if (cachedClassInfo == null) {
            cachedClassInfo = new HashMap<String, IterationPrimaryClassInfo>();

            InputStream is;
            try {
                is = new FileInputStream(IPCI_DATA_DUMP_FILE);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
            
            Deserializer<IterationPrimaryClassInfo> d
                = Deserializer.getDeserializer(is,
                                               IterationPrimaryClassInfo.class);

            d.process(new TraceProcessor<IterationPrimaryClassInfo>() {
                    public void processEvent(IterationPrimaryClassInfo ipci) {
                        addEntryToCache(ipci);
                    }});
        }
    }

    private static void addEntryToCache(IterationPrimaryClassInfo ipci) {
        String name = Misc.classNameSlashesToPeriods(ipci.className);
        cachedClassInfo.put(name, ipci);
    }

    public static boolean isIterationPrimaryClass(String name) {
        initializeCache();

        return cachedClassInfo.containsKey(name);
    }
    
    public static IterationPrimaryClassInfo getClassInfo(String name) {
        initializeCache();

        return cachedClassInfo.get(name);
    }

    private static void saveData() throws FileNotFoundException {
        initializeCache();

        OutputStream os = new FileOutputStream(IPCI_DATA_DUMP_FILE);
        Serializer<IterationPrimaryClassInfo> s = Serializer.getSerializer(os);

        for (IterationPrimaryClassInfo ipci : cachedClassInfo.values()) {
            s.write(ipci);
        }
        
        s.close();
    }

    /**
     * Returns a IterationPrimaryClassInfo object for the given class,
     * with all methods listed; this is intended as a
     * skeleton which can be edited to contain the correct data.
     */
    private static IterationPrimaryClassInfo createSampleIPCI(String className)
        throws ClassNotFoundException {
        IterationPrimaryClassInfo ipci = new IterationPrimaryClassInfo(className);

        Class<?> c = Class.forName(Misc.classNameSlashesToPeriods(className));

        ipci.reflectivelyFillMethods(c);
        
        return ipci;
    }

    private void reflectivelyFillMethods(Class<?> c) {
        for (Method m : c.getMethods()) {
            // static methods are irrelevant
            if ((m.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }

            // This is supposed to represent accesses across a
            // boundary, so only care about public methods.
            if ((m.getModifiers() & Modifier.PUBLIC) == 0) {
                continue;
            }

            if (m.getName().equals("<init>")) {
                continue;
            }

            TraceMethod tm = TraceMethod.createFromMethod(m);

            if (tm.name.startsWith("next")) {
                nextItemMethods.add(tm);
            } else {
                benignMethods.add(tm);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("usage: java IterationPrimaryClassInfo pack/age/classname");
        }

        initializeCache();
        IterationPrimaryClassInfo ipci = createSampleIPCI(args[0]);
        addEntryToCache(ipci);
        saveData();
    }
}