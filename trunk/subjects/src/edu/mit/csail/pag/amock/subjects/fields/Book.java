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
        System.err.println("You read the book out loud!");
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            for (StackTraceElement ste : e.getStackTrace()) {
                if (ste.getClassName().equals("junit.framework.TestSuite")) {
                    throw new RuntimeException("should have been mocked out!");
                }
            }
        }
    }
}
