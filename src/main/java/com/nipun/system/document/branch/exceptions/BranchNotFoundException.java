package com.nipun.system.document.branch.exceptions;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException() {
        super("The requested branch is not available for the current document.");
    }
}
