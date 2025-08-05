package com.nipun.system.document.exceptions;

public class DocumentVersionNotFoundException extends RuntimeException {
    public DocumentVersionNotFoundException() {
        super("No such document version own or shard with user");
    }
}
