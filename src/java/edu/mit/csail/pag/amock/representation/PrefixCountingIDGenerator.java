package edu.mit.csail.pag.amock.representation;

public class PrefixCountingIDGenerator implements IDGenerator {
    private final String prefix;
    private Integer count = null;
    
    public PrefixCountingIDGenerator(String prefix) {
        this.prefix = prefix;
    }

    public String getNextID() {
        if (count == null) {
            count = 1;
        } else {
            count++;
        }
        return getLastID();
    }

    public String getLastID() {
        if (count == null) {
            return null;
        } else {
            return prefix + count;
        }
    }
}