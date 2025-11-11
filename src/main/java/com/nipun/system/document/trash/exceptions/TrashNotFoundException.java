package com.nipun.system.document.trash.exceptions;

public class TrashNotFoundException extends RuntimeException {
    public TrashNotFoundException() {
        super("Trash not found");
    }

    public TrashNotFoundException(String message) {
        super(message);
    }
}
