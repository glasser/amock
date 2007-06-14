package edu.mit.csail.pag.amock.hooks;

import edu.mit.csail.pag.amock.representation.*;
import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

import java.util.*;
import java.io.*;

public abstract class StaticFieldPrimaryClassInfo implements Serializable {
    // the interface or class itself.
    public final ClassName className;

    private static final String SFPCI_DATA_DUMP_FILE
        = "src/java/edu/mit/csail/pag/amock/hooks/static-field-primary-data.xml";

    public StaticFieldPrimaryClassInfo(ClassName className) {
        this.className = className;
    }

    abstract public boolean isSafeStaticField(TraceField f);
    
    private static Map<ClassName, StaticFieldPrimaryClassInfo> cachedClassInfo;

    private static void initializeCache() {
        if (cachedClassInfo == null) {
            cachedClassInfo = new HashMap<ClassName, StaticFieldPrimaryClassInfo>();

            InputStream is;
            try {
                is = new FileInputStream(SFPCI_DATA_DUMP_FILE);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
            
            Deserializer<StaticFieldPrimaryClassInfo> d
                = new XMLDeserializer<StaticFieldPrimaryClassInfo>(is,
                                                                   StaticFieldPrimaryClassInfo.class);

            d.process(new TraceProcessor<StaticFieldPrimaryClassInfo>() {
                    public void processEvent(StaticFieldPrimaryClassInfo sfpci) {
                        addEntryToCache(sfpci);
                    }});
        }
    }

    private static void addEntryToCache(StaticFieldPrimaryClassInfo sfpci) {
        cachedClassInfo.put(sfpci.className, sfpci);
    }

    public static boolean isStaticFieldPrimaryClass(ClassName name) {
        initializeCache();
        
        return cachedClassInfo.containsKey(name);
    }

    public static StaticFieldPrimaryClassInfo getClassInfo(ClassName name) {
        initializeCache();

        return cachedClassInfo.get(name);
    }

    private static void saveData() throws FileNotFoundException {
        initializeCache();

        OutputStream os = new FileOutputStream(SFPCI_DATA_DUMP_FILE);
        Serializer<StaticFieldPrimaryClassInfo> s
            = Serializer.getSerializer(os);

        for (StaticFieldPrimaryClassInfo sfpci : cachedClassInfo.values()) {
            s.write(sfpci);
        }
        
        s.close();
    }

    /**
     * Returns a StaticFieldPrimaryClassInfo object for the given class,
     * with all static final fields listed; this is intended as a
     * skeleton which can be edited to contain the correct data.
     */
    private static StaticFieldPrimaryClassInfo createSampleSFPCI(ClassName className)
        throws ClassNotFoundException {
        SFPCIList sfpci = new SFPCIList(className);

        Class<?> c = Class.forName(className.dotted());

        sfpci.reflectivelyFillFields(c);
        
        return sfpci;
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("usage: java StaticFieldPrimaryClassInfo pack/age/classname");
        }

        initializeCache();
        StaticFieldPrimaryClassInfo sfpci = createSampleSFPCI(ClassName.fromSlashed(args[0]));
        addEntryToCache(sfpci);
        saveData();
    }
}