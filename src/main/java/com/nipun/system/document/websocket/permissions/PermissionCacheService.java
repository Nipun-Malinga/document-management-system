package com.nipun.system.document.websocket.permissions;

import java.util.UUID;

public interface PermissionCacheService {
    void setUserPermissions(UUID documentId, Long UserId, Permissions options);

    Permissions getUserPermissions(UUID documentId, Long userId);

    void removePermission(UUID documentId, Long userId);
}
