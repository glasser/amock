package edu.mit.csail.pag.amock.subjects;

public class PositiveIntBoxSystemTest {
    public static void main(String[] args) {
        PositiveIntBox p1, p2, p3;

        p1 = new PositiveIntBox(24);
        p2 = new PositiveIntBox(26);
        p2.set(43);
        p1.set(52);
        p2.set(12);
        System.out.println(p1.get());
        System.out.println(p2.get());
        assert p1.get() == 52;
        p3 = new PositiveIntBox(150);
        p3.set(p2.get());
        System.out.println(p3.get());
    }
}