package edu.mit.csail.pag.amock.subjects.fields;

import edu.mit.csail.pag.amock.subjects.Util;

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
        System.err.println("You read the book out loud!");
        Util.mustBeMockedOut();
    }
}
