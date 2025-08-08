package com.nipun.system.document.exceptions;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super("The requested document is not available for the current user.");
    }
}
