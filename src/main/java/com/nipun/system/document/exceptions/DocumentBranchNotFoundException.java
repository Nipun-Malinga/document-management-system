package com.nipun.system.document.exceptions;

public class DocumentBranchNotFoundException extends RuntimeException {
    public DocumentBranchNotFoundException() {
        super("The requested branch is not available for the current document.");
    }
}
