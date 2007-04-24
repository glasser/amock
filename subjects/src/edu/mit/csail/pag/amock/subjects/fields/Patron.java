package edu.mit.csail.pag.amock.subjects.fields;

public class Patron {
    public void browseAndCheckOut(Library l) {
        Book b = l.browse();
        l.checkOut(b.title);
    }
}
