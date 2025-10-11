package com.nipun.system.document.websocket.permissions;

import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.share.SharedDocumentAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionCacheService permissionCacheService;
    private final DocumentRepository documentRepository;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    private final Object cacheMonitor = new Object();

    @Override
    public boolean isUnauthorizedUser(UUID documentId, Long userId) {
        return getPermissions(documentId, userId).isUnauthorizedUser();
    }

    @Override
    public boolean isReadOnlyUser(UUID documentId, Long userId) {
        return getPermissions(documentId, userId).isReadOnlyUser();
    }

    @Override
    public void removeUserPermissions(UUID documentId, Long userId) {
        permissionCacheService.removePermission(documentId, userId);
    }

    private Permissions getPermissions(UUID documentId, Long userId) {
        var permissions = permissionCacheService.getUserPermissions(documentId, userId);

        if (permissions != null)
            return permissions;

        synchronized (cacheMonitor) {
            permissions = permissionCacheService.getUserPermissions(documentId, userId);

            if (permissions == null) {
                var document = documentRepository.findByPublicId(documentId)
                        .orElseThrow(DocumentNotFoundException::new);

                var unauthorized = sharedDocumentAuthService.isUnauthorizedUser(userId, document);
                var readOnly = sharedDocumentAuthService.isReadOnlyUser(userId, document);

                permissions = new Permissions(unauthorized, readOnly);

                permissionCacheService.setUserPermissions(documentId, userId, permissions);
            }
        }

        return permissions;
    }
}
