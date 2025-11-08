package com.nipun.system.filemanager.exceptions;

public class FileUploadFailedException extends RuntimeException {

    public FileUploadFailedException() {
        super("Failed to upload file to the cloud");
    }

    public FileUploadFailedException(String message) {
        super(message);
    }
}
