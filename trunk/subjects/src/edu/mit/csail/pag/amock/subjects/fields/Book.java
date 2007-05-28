package edu.mit.csail.pag.amock.subjects.fields;

public class Book {
    public String title;

    public Book(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void readOutLoud() {
        System.err.println("THIS MAY NOT BE PRINTED DURING THE MOCK TEST!");
    }
}
