package com.nipun.system.document.branch.exceptions;

public class BranchTitleAlreadyExistsException extends RuntimeException {
    public BranchTitleAlreadyExistsException() {
        super("The entered branch title already exists in the document");
    }
}
