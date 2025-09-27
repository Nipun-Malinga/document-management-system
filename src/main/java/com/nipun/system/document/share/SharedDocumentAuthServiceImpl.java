package com.nipun.system.document.share;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SharedDocumentAuthServiceImpl implements SharedDocumentAuthService {

    @Override
    public void checkUserCanWrite(Long userId, Document document) {
        if (isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        if (isReadOnlyUser(userId, document))
            throw new ReadOnlyDocumentException();
    }

    @Override
    public boolean isUnauthorizedUser(Long userId, Document document) {
        return !isOwner(userId, document) &&  !isSharedUser(userId, document);
    }

    @Override
    public boolean isReadOnlyUser(Long userId, Document document) {
        return document.getSharedUsers()
                .stream()
                .anyMatch(user ->
                        Objects.equals(user.getUserId(), userId) && user.isReadOnlyUser());
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
