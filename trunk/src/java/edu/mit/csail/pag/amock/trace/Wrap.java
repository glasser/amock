package edu.mit.csail.pag.amock.trace;

public final class Wrap {

    ///////////////////////////////////////////////////////////////////////////
    /// Wrappers for the various primitive types.
    /// Used to distinguish wrappers created by user code
    /// from wrappers created by Trace.

    public static interface PrimitiveWrapper
    {
      public String type();
    }

    /** wrapper used for boolean arguments **/
    public static final class BooleanWrap implements PrimitiveWrapper{
      boolean val;
      public BooleanWrap (boolean val) { this.val = val; }
      @Override
      public String toString() {return Boolean.toString(val);}
      public String type() {return "boolean";}
    }

    /** wrapper used for int arguments **/
    public static final class ByteWrap implements PrimitiveWrapper{
      byte val;
      public ByteWrap (byte val) { this.val = val; }
      @Override
      public String toString() {return Byte.toString(val);}
      public String type() {return "byte";}
    }

    /** wrapper used for int arguments **/
    public static final class CharWrap implements PrimitiveWrapper{
      char val;
      public CharWrap (char val) { this.val = val; }
      // Print characters as integers.
      @Override
      public String toString() {return Integer.toString(val);}
      public String type() {return "char";}
    }

    /** wrapper used for int arguments **/
    public static final class FloatWrap implements PrimitiveWrapper{
      float val;
      public FloatWrap (float val) { this.val = val; }
      @Override
      public String toString() {return Float.toString(val);}
      public String type() {return "float";}
    }

    /** wrapper used for int arguments **/
    public static final class IntWrap implements PrimitiveWrapper{
      int val;
      public IntWrap (int val) { this.val = val; }
      @Override
      public String toString() {return Integer.toString(val);}
      public String type() {return "int";}
    }

    /** wrapper used for int arguments **/
    public static final class LongWrap implements PrimitiveWrapper{
      long val;
      public LongWrap (long val) { this.val = val; }
      @Override
      public String toString() {return Long.toString(val);}
      public String type() {return "long";}
    }

    /** wrapper used for int arguments **/
    public static final class ShortWrap implements PrimitiveWrapper{
      short val;
      public ShortWrap (short val) { this.val = val; }
      @Override
      public String toString() {return Short.toString(val);}
      public String type() {return "short";}
    }

    /** wrapper used for double arguments **/
    public static final class DoubleWrap implements PrimitiveWrapper{
      double val;
      public DoubleWrap (double val) { this.val = val; }
      @Override
      public String toString() {return Double.toString(val);}
          public String type() {return "double";}

    }

    /** Used for functions that return void **/
    public static final class VoidWrap { /*no code*/
    }
    
    /** Used as the return value for functions that return void **/
    public static final VoidWrap void_obj = new VoidWrap();
}
