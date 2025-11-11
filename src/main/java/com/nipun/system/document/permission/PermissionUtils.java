package com.nipun.system.document.permission;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.permission.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;

import java.util.Objects;

public class PermissionUtils {
    public static void checkUserCanWrite(Long userId, Document document) {
        if (isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        if (isReadOnlyUser(userId, document))
            throw new ReadOnlyDocumentException();
    }

    public static boolean isUnauthorizedUser(Long userId, Document document) {
        return !isOwner(userId, document) && !isSharedUser(userId, document);
    }

    public static boolean isReadOnlyUser(Long userId, Document document) {
        return document.getSharedUsers()
                .stream()
                .anyMatch(user ->
                        Objects.equals(user.getUserId(), userId) && user.isReadOnlyUser());
    }

    public static boolean isOwner(Long userId, Document document) {
        return Objects.equals(document.getOwner().getId(), userId);
    }

    private static boolean isSharedUser(Long userId, Document document) {
        return document.getSharedUsers()
                .stream()
                .anyMatch(user -> Objects.equals(user.getUserId(), userId));
    }
}
