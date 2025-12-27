package com.nipun.system.document.folder.exceptions;

public class FolderNotFoundException extends RuntimeException {

    public FolderNotFoundException() {
        super("Folder not found");
    }

    public FolderNotFoundException(String message) {
        super(message);
    }
}
