package edu.mit.csail.pag.amock.subjects.fields;

public class Patron {
    public void browseAndCheckOut(Library l, boolean readIt) {
        Book b = l.browse();
        l.checkOut(b.title);

        // The idea is: readOutLoud is not a benign method, and needs
        // to be mocked, so iterations with readIt true should have
        // Book be a mock, whereas iterations with readIt false should
        // have Book be a RecordPrimary.
        if (readIt) {
            b.readOutLoud();
        }
    }
}
