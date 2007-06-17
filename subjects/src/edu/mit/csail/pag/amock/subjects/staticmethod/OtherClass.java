package edu.mit.csail.pag.amock.subjects.staticmethod;

public class OtherClass {
    public static String getSomeNumber() {
//         try {
//             throw new RuntimeException();
//         } catch (RuntimeException e) {
//             for (StackTraceElement ste : e.getStackTrace()) {
//                 if (ste.getClassName().equals("junit.framework.TestSuite")) {
//                     throw new RuntimeException("should have been mocked out!");
//                 }
//             }
//         }

        return "hi";

    }
}
