package io.github.x0b.safdav.file;

public class ItemNotFoundException extends SafException {
    public ItemNotFoundException() {
        super("Item not found or no longer accessible for user");
    }

    public ItemNotFoundException(Throwable cause) {
        super(cause);
    }
}
