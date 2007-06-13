package edu.mit.csail.pag.amock.subjects.fields;

public class Singleton {
    public static final Singleton INSTANCE = new Singleton("secret");

    private final String field;
    
    private Singleton(String field) {
        this.field = field;
    }

    public String getField() {
        return this.field;
    }
}
