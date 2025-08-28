package com.nipun.system.shared.exceptions;

public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException() {
        super("Invalid or expired Jwt token detected.");
    }
}
