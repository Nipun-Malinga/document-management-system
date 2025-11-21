package com.nipun.system.auth.exceptions;

public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException() {
        super("Invalid or expired Jwt token detected.");
    }
}
