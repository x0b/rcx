package io.github.x0b.safdav.file;

public class ItemExistsException extends SafException {
    public ItemExistsException() {
        super("Item already exists");
    }
}
