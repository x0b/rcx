package io.github.x0b.safdav.file;

import io.github.x0b.safdav.file.SafException;

/**
 * A general file access error, often a result of an {@link java.io.IOException}.
 */
public class FileAccessError extends SafException {

    /**
     * Create a new FileAccessError with cause
     * @param cause the actual Throwable
     */
    public FileAccessError(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new FileAccessError without a specific cause
     */
    public FileAccessError() {
        super("Could not access file");
    }
}
