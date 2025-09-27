package com.nipun.system.document.version.exceptions;

public class VersionNotFoundException extends RuntimeException {
    public VersionNotFoundException() {
        super("The requested document version could not be found or is not accessible to you.");
    }
}
