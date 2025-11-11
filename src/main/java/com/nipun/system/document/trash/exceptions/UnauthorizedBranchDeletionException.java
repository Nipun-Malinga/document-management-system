package com.nipun.system.document.trash.exceptions;

public class UnauthorizedBranchDeletionException extends RuntimeException {
    public UnauthorizedBranchDeletionException() {
        super("Unauthorized branch deletion");
    }

    public UnauthorizedBranchDeletionException(String message) {
        super(message);
    }
}
