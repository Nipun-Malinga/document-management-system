package com.nipun.system.shared.exceptions;

import com.nipun.system.auth.exceptions.BadCredentialsException;
import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.user.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleRequestBodyException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(new ErrorResponse(errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestDataException() {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("Invalid request datatype"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("User not found"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid Credentials"));
    }
}
