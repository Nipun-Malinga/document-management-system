package com.nipun.system.document.share.exceptions;

public class ReadOnlyDocumentException extends RuntimeException {
    public ReadOnlyDocumentException() {
        super("You do not have permission to modify this document. It is read-only.");
    }
}
