package edu.mit.csail.pag.smock;

import java.io.*;
import java.util.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;

public class Premain implements ClassFileTransformer {
    public static void premain(String agentArgs, Instrumentation inst) {
        Premain p = new Premain();
        inst.addTransformer(p);
    }
    
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        boolean definitely = false;
        for (String p : edu.mit.csail.pag.amock.trace.Premain.transformAnywayPrefixes) {
            if (className.startsWith(p)) {
                definitely = true;
            }
        }
        
        if (!definitely) {
            for (String p : edu.mit.csail.pag.amock.trace.Premain.nonTransformedPrefixes) {
                if (className.startsWith(p)) {
                    return null;
                }
            }
        }

        ClassWriter cw = new ClassWriter(true);
        ClassVisitor transformer = new SmockTransformer(cw);
        ClassReader cr = new ClassReader(classfileBuffer);
        cr.accept(transformer, true);
        byte[] transformed = cw.toByteArray();

        if (className.equals("org/hamcrest/core/IsAnything")) {
            try { FileOutputStream p = new FileOutputStream("ia.class");
                p.write(transformed);
                p.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return transformed;
    }
}

