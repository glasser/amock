package edu.mit.csail.pag.amock.processor;

import java.io.*;

import edu.mit.csail.pag.amock.trace.*;
import edu.mit.csail.pag.amock.representation.*;

public class Processor {
    private static final String TEST_CASE_NAME = "CookieMonsterTest";
    private static final String TEST_METHOD_NAME = "cookieEating";
    
    private TestCaseGenerator testCaseGenerator;
    private TestMethodGenerator testMethodGenerator;
    private final Deserializer deserializer;

    private State state = new InitialState();

    public Processor(Deserializer deserializer) {
        this.deserializer = deserializer;

        testCaseGenerator = new TestCaseGenerator(TEST_CASE_NAME);
        testMethodGenerator =
            new TestMethodGenerator(TEST_METHOD_NAME, testCaseGenerator);
        testCaseGenerator.addChunk(testMethodGenerator);
    }

    public void process() {
        while (true) {
            TraceEvent ev = deserializer.read();

            if (ev == null) {
                break;
            }

            state.process(ev);
        }
    }

    public void print() {
        testCaseGenerator.printSource(new PrintStreamLinePrinter(System.out));
    }

    private interface State {
        public void process(TraceEvent ev);
    }

    private class InitialState implements State {
        public void process(TraceEvent ev) {
            System.err.println("hi: " + ev.getClass().getName());
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        Deserializer d = new Deserializer(new FileInputStream(args[0]));

        Processor p = new Processor(d);
        p.process();
        p.print();
    }
}