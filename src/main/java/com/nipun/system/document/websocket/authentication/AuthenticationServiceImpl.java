package com.nipun.system.document.websocket.authentication;

import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.cache.DocumentCacheService;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.share.SharedDocumentAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final DocumentRepository documentRepository;


    private final DocumentCacheService documentCacheService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    @Override
    public boolean isUnauthorizedUser(Long userId, UUID documentId) {
        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            return userPermissions.get(userIdCacheKey).isUnAuthorizedUser();
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (userPermissions == null) {
            userPermissions = new ConcurrentHashMap<>();
        }

        var isUnauthorized = sharedDocumentAuthService.isUnauthorizedUser(userId, document);
        var isReadOnlyUser = sharedDocumentAuthService.isReadOnlyUser(userId, document);

        userPermissions.put(userIdCacheKey, new AuthorizedOptions(isUnauthorized, isReadOnlyUser));

        documentCacheService.putDocumentUserPermissions(documentId, userPermissions);

        return isUnauthorized;
    }

    @Override
    public boolean isReadOnlyUser(Long userId, UUID documentId) {
        var userIdKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        return userPermissions != null &&
                userPermissions.containsKey(userIdKey) &&
                userPermissions.get(userIdKey).isReadOnlyUser();
    }


    @Override
    public void updateDocumentPermissionDetails(UUID documentId, Long userId, AuthorizedOptions authorizedOptions) {

        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            userPermissions.put(userIdCacheKey, authorizedOptions);
            documentCacheService.putDocumentUserPermissions(documentId, userPermissions);
        }
    }

    @Override
    public void removeDocumentPermissionDetailsFromCache(UUID documentId, Long userId) {

        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            userPermissions.remove(userIdCacheKey);
            documentCacheService.putDocumentUserPermissions(documentId, userPermissions);
        }
    }
}
