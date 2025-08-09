package com.nipun.system.document.exceptions;

public class DocumentVersionNotFoundException extends RuntimeException {
    public DocumentVersionNotFoundException() {
        super("The requested document version could not be found or is not accessible to you.");
    }
}
