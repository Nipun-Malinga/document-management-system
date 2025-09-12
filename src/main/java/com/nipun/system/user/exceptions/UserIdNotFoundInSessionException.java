package com.nipun.system.user.exceptions;

public class UserIdNotFoundInSessionException extends RuntimeException {
    public UserIdNotFoundInSessionException(String sessionId) {
        super("User Id not found in the session: " + sessionId);
    }
}
