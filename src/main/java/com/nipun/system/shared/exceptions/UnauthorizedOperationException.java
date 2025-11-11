package com.nipun.system.shared.exceptions;

public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException() {
        super("Unauthorized operation");
    }

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
