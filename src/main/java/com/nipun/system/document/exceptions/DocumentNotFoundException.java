package com.nipun.system.document.exceptions;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super("Requested document was not found for the current user.");
    }
}
