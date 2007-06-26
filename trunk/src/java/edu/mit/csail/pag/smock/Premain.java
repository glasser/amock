package edu.mit.csail.pag.smock;

import java.io.*;
import java.util.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;

import edu.mit.csail.pag.amock.util.ClassName;

public class Premain implements ClassFileTransformer {
    public static void premain(String agentArgs, Instrumentation inst) {
        Premain p = new Premain();
        inst.addTransformer(p);
    }
    
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        ClassName cn = ClassName.fromSlashed(className);
        if (! edu.mit.csail.pag.amock.trace.Premain.shouldTransform(cn)) {
            return null;
        }

        ClassWriter cw = new ClassWriter(true);
        ClassVisitor transformer = new SmockTransformer(cw);
        ClassReader cr = new ClassReader(classfileBuffer);
        cr.accept(transformer, true);
        byte[] transformed = cw.toByteArray();

        return transformed;
    }
}

