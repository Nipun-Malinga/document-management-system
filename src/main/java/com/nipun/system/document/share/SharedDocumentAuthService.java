package com.nipun.system.document.share;

import com.nipun.system.document.Document;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SharedDocumentAuthService {

    public void checkUserCanWrite(Long userId, Document document) {
        if (isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        if (isReadOnlyUser(userId, document))
            throw new ReadOnlyDocumentException();
    }

    public boolean isUnauthorizedUser(Long userId, Document document) {
        return !isOwner(userId, document) &&  !isSharedUser(userId, document);
    }

    public boolean isReadOnlyUser(Long userId, Document document) {
        return document.getSharedUsers()
                .stream()
                .anyMatch(user ->
                        Objects.equals(user.getUserId(), userId) &&
                                user.getPermission() == Permission.READ_ONLY);
    }

    private boolean isOwner(Long userId, Document document) {
        return Objects.equals(document.getOwner().getId(), userId);
    }

    private boolean isSharedUser(Long userId, Document document) {
        return document.getSharedUsers()
                .stream()
                .anyMatch(user -> Objects.equals(user.getUserId(), userId));
    }
}
