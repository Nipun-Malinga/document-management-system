package com.nipun.system.shared.exceptions;

public class JwtTokenNotFoundException extends RuntimeException {
    public JwtTokenNotFoundException() {
        super("Jwt token not found");
    }
}
