package com.nipun.system.document.websocket.authentication;

import java.util.UUID;

public interface DocumentWebsocketAuthenticationService {
    boolean isUnauthorizedUser(Long userId, UUID documentId);

    boolean isReadOnlyUser(Long userId, UUID documentId);

    void updateDocumentPermissionDetails(UUID documentId, Long userId, AuthorizedOptions authorizedOptions);

    void removeDocumentPermissionDetailsFromCache(UUID documentId, Long userId);
}
