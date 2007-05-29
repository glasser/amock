package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

import java.util.*;
import java.io.*;

public class RecordPrimaryClassInfo {
    // with slashes.
    public final String className;

    public final Map<TraceField, Integer> fieldSlots
        = new HashMap<TraceField, Integer>();

    public final Map<TraceMethod, Integer> methodSlots
        = new HashMap<TraceMethod, Integer>();

    // This assumes just one constructor; not a big deal for now.
    public final List<ProgramObject> slotDefaults
        = new ArrayList<ProgramObject>();

    public final Set<TraceMethod> benignMethods
        = new HashSet<TraceMethod>();

    private static final String RPCI_DATA_DUMP_FILE
        = "src/java/edu/mit/csail/pag/amock/representation/record-primary-data.xml";

    public RecordPrimaryClassInfo(String className) {
        this.className = className;
    }

    // Note that the keys of this map have periods, not slashes.
    private static Map<String, RecordPrimaryClassInfo> cachedClassInfo;

    private static void initializeCache() {
        if (cachedClassInfo == null) {
            cachedClassInfo = new HashMap<String, RecordPrimaryClassInfo>();

            InputStream is;
            try {
                is = new FileInputStream(RPCI_DATA_DUMP_FILE);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
            
            Deserializer<RecordPrimaryClassInfo> d
                = Deserializer.getDeserializer(is,
                                               RecordPrimaryClassInfo.class);

            while (true) {
                RecordPrimaryClassInfo rpci = d.read();

                if (rpci == null) {
                    break;
                }

                addEntryToCache(rpci);
            }
        }
    }

    private static void addEntryToCache(RecordPrimaryClassInfo rpci) {
        String name = Misc.classNameSlashesToPeriods(rpci.className);
        cachedClassInfo.put(name, rpci);
    }

    public static boolean isRecordPrimaryClass(String name) {
        initializeCache();

        return cachedClassInfo.containsKey(name);
    }
    
    public static RecordPrimaryClassInfo getClassInfo(String name) {
        initializeCache();

        return cachedClassInfo.get(name);
    }

    private static void saveData() throws FileNotFoundException {
        initializeCache();

        OutputStream os = new FileOutputStream(RPCI_DATA_DUMP_FILE);
        Serializer<RecordPrimaryClassInfo> s = Serializer.getSerializer(os);

        for (RecordPrimaryClassInfo rpci : cachedClassInfo.values()) {
            s.write(rpci);
        }
        
        s.close();
    }

    /**
     * Returns a RecordPrimaryClassInfo object for the given class,
     * with all fields and methods listed; this is intended as a
     * skeleton which can be edited to contain the correct data.
     */
    private static RecordPrimaryClassInfo createSampleRPCI(String className) {
        RecordPrimaryClassInfo rpci = new RecordPrimaryClassInfo(className);

        //NEXT: fill in blanks, with reflection
        
        return rpci;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("usage: java RecordPrimaryClassInfo pack/age/classname");
        }

        initializeCache();
        RecordPrimaryClassInfo rpci = createSampleRPCI(args[0]);
        addEntryToCache(rpci);
        saveData();
    }
}