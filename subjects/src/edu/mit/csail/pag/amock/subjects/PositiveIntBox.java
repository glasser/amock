package edu.mit.csail.pag.amock.subjects;

public class PositiveIntBox {
    private int myInt;

    public PositiveIntBox(int myInt) {
        this.myInt = myInt;
    }

    public int get() {
        return myInt;
    }

    public void set(int myInt) {
        this.myInt = myInt;
    }

    public void increment() {
        int x = get();
        set(x+1);
    }

    public void addBoxed(PositiveIntBox other) {
        this.myInt += other.get();
    }
}