package edu.mit.csail.pag.amock.subjects.fields;

public class Library {
    private final Book book;

    public Library(Book book) {
        this.book = book;
    }

    public Book browse() {
        return book;
    }

    public void checkOut(String title) {
        if (!title.equals(book.title)) {
            throw new RuntimeException("We don't have that book!");
        }
    }
}
