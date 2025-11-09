package com.nipun.system.document.permission.exceptions;

public class UnauthorizedDocumentException extends RuntimeException {
    public UnauthorizedDocumentException() {
        super("The requested document version could not be found or is not accessible to you.");
    }
}
