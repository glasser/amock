package edu.mit.csail.pag.amock.trace;

import org.objectweb.asm.*;

public class TraceTransformer extends ClassAdapter {
  public TraceTransformer(ClassVisitor cv) {
    super(cv);
  }
}