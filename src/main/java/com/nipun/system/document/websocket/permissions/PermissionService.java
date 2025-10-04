package com.nipun.system.document.websocket.permissions;

import java.util.UUID;

public interface PermissionService {
    boolean isUnauthorizedUser(UUID documentId, Long userId);

    boolean isReadOnlyUser(UUID documentId, Long userId);

    void removeUserPermissions(UUID documentId, Long userId);
}
