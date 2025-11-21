package com.nipun.system.auth.exceptions;

public class JwtTokenNotFoundException extends RuntimeException {
    public JwtTokenNotFoundException() {
        super("Jwt token not found");
    }
}
