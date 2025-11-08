package com.nipun.system.filemanager.exceptions;

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException() {
        super("Invalid file type");
    }

    public InvalidFileTypeException(String message) {
        super(message);
    }
}
