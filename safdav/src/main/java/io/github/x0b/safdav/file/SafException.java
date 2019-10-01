package io.github.x0b.safdav.file;

public class SafException extends RuntimeException {

    public SafException() {
        super("Unknown Error");
    }

    public SafException(String message){
        super(message);
    }

    public SafException(Throwable cause) {
        super(cause);
    }
}
