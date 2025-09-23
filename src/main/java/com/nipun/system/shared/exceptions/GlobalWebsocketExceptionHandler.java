package com.nipun.system.shared.exceptions;

import com.nipun.system.auth.exceptions.BadCredentialsException;
import com.nipun.system.document.exceptions.*;
import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.user.exceptions.UserIdNotFoundInSessionException;
import com.nipun.system.user.exceptions.UserNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice
public class GlobalWebsocketExceptionHandler {

    @MessageExceptionHandler(InvalidJwtTokenException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleInvalidJwt(
            InvalidJwtTokenException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(JwtTokenNotFoundException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleJwtTokenNotFoundException(
            JwtTokenNotFoundException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(UnauthorizedDocumentException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleUnauthorizedDocumentException(
            UnauthorizedDocumentException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(UserNotFoundException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleUserNotFoundException() {
        return new ErrorResponse("User not found");
    }

    @MessageExceptionHandler(BadCredentialsException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleBadCredentialsException() {
        return new ErrorResponse("Invalid Credentials");
    }

    @MessageExceptionHandler(HttpMessageNotReadableException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleHttpMessageNotReadableException() {
        return new ErrorResponse("Malformed JSON or missing fields");
    }

    @MessageExceptionHandler(DocumentNotFoundException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleDocumentNotFoundException(
            DocumentNotFoundException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(ReadOnlyDocumentException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleReadOnlyDocumentException(
            ReadOnlyDocumentException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(DocumentVersionNotFoundException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleDocumentVersionNotFoundException(
            DocumentVersionNotFoundException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(PatchFailedException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handlePatchFailedException(
            PatchFailedException exception
    ) {
        return new ErrorResponse(exception.getMessage());
    }

    @MessageExceptionHandler(UserIdNotFoundInSessionException.class)
    public void handleUserIdNotFoundInSessionException(
            UserIdNotFoundInSessionException exception
    ) {
        System.out.println(exception.getMessage());
    }
}
