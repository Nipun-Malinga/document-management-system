package com.nipun.system.document.exceptions;

public class DocumentVersionNotFoundException extends RuntimeException {
    public DocumentVersionNotFoundException(String message) {
        super(message);
    }
}
