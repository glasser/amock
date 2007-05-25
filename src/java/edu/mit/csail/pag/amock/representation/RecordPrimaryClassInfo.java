package edu.mit.csail.pag.amock.representation;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.util.*;

import java.util.*;
import java.io.*;

public class RecordPrimaryClassInfo {
    // with slashes.
    public final String className;

    public final Map<TraceField, Integer> fieldSlots;

    public final Map<TraceMethod, Integer> methodSlots;

    public final List<ProgramObject> slotDefaults;


    private static final String RPCI_DATA_DUMP_FILE
        = "src/java/edu/mit/csail/pag/amock/representation/record-primary-data.xml";

    public RecordPrimaryClassInfo(String className,
                                  Map<TraceField, Integer> fieldSlots,
                                  Map<TraceMethod, Integer> methodSlots,
                                  List<ProgramObject> slotDefaults) {
        this.className = className;
        this.fieldSlots = fieldSlots;
        this.methodSlots = methodSlots;
        this.slotDefaults = slotDefaults;
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

                String name = Misc.classNameSlashesToPeriods(rpci.className);
                cachedClassInfo.put(name, rpci);
            }
        }
    }

    public static boolean isRecordPrimaryClass(String name) {
        initializeCache();

        return cachedClassInfo.containsKey(name);
    }
    
    public static RecordPrimaryClassInfo getClassInfo(String name) {
        initializeCache();

        return cachedClassInfo.get(name);
    }   

    public static void main(String[] args) throws Exception {
        Map<TraceField, Integer> RECTANGLE_FIELDS
            = new HashMap<TraceField, Integer>();
        Map<TraceMethod, Integer> MOUSEEVENT_METHODS
            = new HashMap<TraceMethod, Integer>();
                RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "x", "I"), 0);
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "y", "I"), 1);
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "width", "I"), 2);
        RECTANGLE_FIELDS.put(new TraceField("java/awt/Rectangle", "height", "I"), 3);

        MOUSEEVENT_METHODS.put(new TraceMethod("java/awt/event/MouseEvent",
                                               "getX", "()I"), 4);
        MOUSEEVENT_METHODS.put(new TraceMethod("java/awt/event/MouseEvent",
                                               "getY", "()I"), 5);
        List<ProgramObject> argValues
            = new ArrayList<ProgramObject>();
        argValues.add(new Primitive(0));
        argValues.add(new Primitive(0));
        argValues.add(new Primitive(0));
        argValues.add(new Primitive(0));

        RecordPrimaryClassInfo rpci
            = new RecordPrimaryClassInfo("java/awt/Rectangle",
                                         RECTANGLE_FIELDS,
                                         MOUSEEVENT_METHODS,
                                         argValues);
        OutputStream os = new FileOutputStream(RPCI_DATA_DUMP_FILE);
        Serializer<RecordPrimaryClassInfo> s = Serializer.getSerializer(os);
        s.write(rpci);
        s.close();
    }

}
