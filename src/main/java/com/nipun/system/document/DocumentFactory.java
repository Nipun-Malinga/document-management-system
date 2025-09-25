package com.nipun.system.document;

import com.nipun.system.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentFactory {
    public static Document createNewDocument(User user, String title) {
        var document = new Document();

        document.setPublicId(UUID.randomUUID());
        document.setOwner(user);
        document.setTitle(title);
        document.setContent(new Content());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        return document;
    }
}
